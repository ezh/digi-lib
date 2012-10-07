/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012 Alexey Aksenov ezh@ezh.msk.ru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.digimead.digi.lib.log

import java.lang.Thread
import java.util.Date
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

import scala.Array.canBuildFrom
import scala.Option.option2Iterable
import scala.annotation.tailrec
import scala.collection.immutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.Publisher
import scala.collection.mutable.SynchronizedMap

import org.digimead.digi.lib.log.appender.Appender
import org.digimead.digi.lib.log.logger.RichLogger
import org.digimead.digi.lib.log.logger.RichLogger.rich2slf4j
import org.slf4j.LoggerFactory

trait Logging {
  /** always call Logging.getRich, even after deserialization */
  @transient
  implicit lazy val log: RichLogger = Logging.getRichLogger(this)
}

object Logging {
  private var logPrefix = "@" // prefix for all adb logcat TAGs, everyone may change (but should not) it on his/her own risk
  private[log] var isTraceExtraEnabled = false
  private[log] var isTraceEnabled = true
  private[log] var isDebugEnabled = true
  private[log] var isInfoEnabled = true
  private[log] var isWarnEnabled = true
  private[log] var isErrorEnabled = true
  private var loggingThread = new Thread() // stub
  private var richLoggerBuilder: (String) => RichLogger = null
  private var flushLimit = 1000
  private var appender = new HashSet[Appender]()
  private var shutdownHook: Thread = null
  private val queue = new ConcurrentLinkedQueue[Record]
  private val richLogger = new HashMap[String, RichLogger]() with SynchronizedMap[String, RichLogger]
  private val initializationArgument = new AtomicReference[Option[Init]](None)
  private val loggingThreadRecords = new Array[Record](flushLimit)
  lazy val commonLogger: RichLogger = {
    val name = if (LoggerFactory.getILoggerFactory.getClass.getName() == "org.digimead.digi.lib.log.LoggerFactory")
      "@~*~*~*~*"
    else
      getClass.getName()
    val logger = new RichLogger(OriginalLoggerFactory.getLogger(name))
    richLogger(name) = logger
    logger
  }

  LoggingInitializationArgument.foreach(init)

  def setTraceEnabled(t: Boolean) = synchronized {
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, commonLogger.getName, if (t)
      "enable TRACE log level"
    else
      "disable TRACE log level"))
    isTraceEnabled = t
  }
  def setDebugEnabled(t: Boolean) = synchronized {
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, commonLogger.getName, if (t)
      "enable DEBUG log level"
    else
      "disable DEBUG log level"))
    isDebugEnabled = t
  }
  def setInfoEnabled(t: Boolean) = synchronized {
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, commonLogger.getName, if (t)
      "enable INFO log level"
    else
      "disable INFO log level"))
    isInfoEnabled = t
  }
  def setWarnEnabled(t: Boolean) = synchronized {
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, commonLogger.getName, if (t)
      "enable WARN log level"
    else
      "disable WARN log level"))
    isWarnEnabled = t
  }
  def setErrorEnabled(t: Boolean) = synchronized {
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, commonLogger.getName, if (t)
      "enable ERROR log level"
    else
      "disable ERROR log level"))
    isErrorEnabled = t
  }
  def offer(record: Record) = queue.synchronized {
    queue.offer(record)
    queue.notifyAll
    try {
      Event.publish(new Event.Incoming(record))
    } catch {
      case e =>
        queue.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName,
          e.getMessage match {
            case null => ""
            case message => message
          }, Some(e)))
    }
  }
  def reset() = synchronized {
    deinit()
    queue.clear()
    initializationArgument.get.foreach(init)
  }
  def init(arg: Init): Unit = synchronized {
    try {
      if (initializationArgument.get.nonEmpty) {
        try {
          Runtime.getRuntime().removeShutdownHook(shutdownHook)
        } catch {
          case e =>
        }
        delAppender(appender.toSeq)
      }
      logPrefix = arg.logPrefix
      isTraceExtraEnabled = arg.isTraceExtraEnabled
      isTraceEnabled = arg.isTraceEnabled
      isDebugEnabled = arg.isDebugEnabled
      isInfoEnabled = arg.isInfoEnabled
      isWarnEnabled = arg.isWarnEnabled
      isErrorEnabled = arg.isErrorEnabled
      richLoggerBuilder = arg.richLoggerBuilder
      flushLimit = arg.flushLimit
      shutdownHook = arg.shutdownHook
      if (LoggerFactory.getILoggerFactory.getClass.getName() == "org.digimead.digi.lib.log.LoggerFactory") {
        Runtime.getRuntime().addShutdownHook(shutdownHook)
        if (initializationArgument.get.nonEmpty)
          resume
        offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName,
          "initialize logging for internal slf4j logger with " + arg))
        initializationArgument.synchronized {
          initializationArgument.set(Some(arg))
          initializationArgument.notifyAll()
        }
        addAppender(arg.appenders)
      } else {
        commonLogger.debug("initialize logging for external slf4j logger with " + arg)
        initializationArgument.synchronized {
          initializationArgument.set(Some(arg))
          initializationArgument.notifyAll()
        }
      }
    } catch {
      case e => try {
        System.err.println(e.getMessage + "\n" + e.getStackTraceString)
      } catch {
        case e =>
        // total destruction
      }
    }
  }
  private[lib] def deinit(): Unit = synchronized {
    offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName, "deinitialize logging"))
    flush()
    delAppender(appender.toSeq)
    queue.clear()
    try {
      Runtime.getRuntime().removeShutdownHook(shutdownHook)
    } catch {
      case e =>
    }
  }
  def suspend() = {
    // non blocking check
    if (loggingThread.isAlive)
      synchronized {
        if (loggingThread.isAlive)
          loggingThread = new Thread() // stub
      }
  }
  def resume() = {
    // non blocking check
    if (!loggingThread.isAlive)
      synchronized {
        if (!loggingThread.isAlive) {
          loggingThread = new Thread("GenericLogger for " + Logging.getClass.getName) {
            this.setDaemon(true)
            @tailrec
            override def run() = {
              if (appender.nonEmpty && !queue.isEmpty) {
                flushQueue(flushLimit)
                Thread.sleep(50)
              } else
                queue.synchronized { queue.wait }
              if (loggingThread.getId == this.getId)
                run
            }
          }
          loggingThread.start
        }
      }
  }
  def flush(): Int = synchronized {
    if (appender.isEmpty)
      return -1
    val flushed = flushQueue()
    appender.foreach(_.flush)
    flushed
  }
  private def flushQueue(): Int = flushQueue(Int.MaxValue)
  @tailrec
  private[log] def flushQueue(n: Int, accumulator: Int = 0): Int = {
    var records = 0
    loggingThreadRecords.synchronized {
      val limit = if (n <= flushLimit) (n - accumulator) else flushLimit
      while (records < limit && !queue.isEmpty()) {
        loggingThreadRecords(records) = queue.poll().asInstanceOf[Record]
        try {
          Event.publish(new Event.Outgoing(loggingThreadRecords(records)))
        } catch {
          case e =>
            offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName,
              e.getMessage match {
                case null => ""
                case message => message
              }, Some(e)))
        }
        records += 1
      }
      appender.foreach(_(loggingThreadRecords.take(records)))
    }
    if (records == flushLimit) {
      if (records == n)
        return accumulator + records
    } else
      return accumulator + records
    Thread.sleep(100)
    flushQueue(n, accumulator + records)
  }
  def dump() =
    ("queue size: " + queue.size + ", appenders: " + appender.mkString(",") + " thread: " + loggingThread.isAlive) +: queue.toArray.map(_.toString)
  def addAppender(s: Seq[Appender]): Unit =
    synchronized { s.foreach(a => addAppender(a, false)) }
  def addAppender(s: Seq[Appender], force: Boolean): Unit =
    synchronized { s.foreach(a => addAppender(a, force)) }
  def addAppender(a: Appender): Unit =
    synchronized { addAppender(a, false) }
  def addAppender(a: Appender, force: Boolean): Unit = synchronized {
    if (!appender.contains(a) || force) {
      initializationArgument.get.foreach(a.init)
      appender = appender + a
      offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName, "add appender " + a))
    }
  }
  def delAppender(s: Seq[Appender]): Unit =
    synchronized { s.foreach(a => delAppender(a)) }
  def delAppender(a: Appender) = synchronized {
    if (appender.contains(a)) {
      offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName, "delete appender " + a))
      flush
      a.flush
      appender = appender - a
      a.deinit
    }
  }
  def getAppenders() = synchronized { appender.toSeq }
  def getRichLogger(obj: Logging): RichLogger = {
    val stackArray = Thread.currentThread.getStackTrace().dropWhile(_.getClassName != getClass.getName)
    val stack = if (stackArray(1).getFileName != stackArray(0).getFileName)
      stackArray(1) else stackArray(2)
    val fileRaw = stack.getFileName.split("""\.""")
    val fileParsed = if (fileRaw.length > 1)
      fileRaw.dropRight(1).mkString(".")
    else
      fileRaw.head
    val loggerName = if (obj.getClass().toString.last == '$') // add object mark to file name
      logPrefix + obj.getClass.getPackage.getName.split("""\.""").last + "." + fileParsed + "$"
    else
      logPrefix + obj.getClass.getPackage.getName.split("""\.""").last + "." + fileParsed
    getRichLogger(loggerName)
  }
  def getRichLogger(name: String): RichLogger =
    if (richLogger.isDefinedAt(name))
      richLogger(name)
    else {
      initializationArgument.synchronized {
        if (initializationArgument.get.isEmpty)
          while (initializationArgument.get.isEmpty)
            initializationArgument.wait()
        val newLogger = richLoggerBuilder(name)
        richLogger(name) = newLogger
        Event.publish(new Event.RegisterLogger(newLogger))
        newLogger
      }
    }
  def findRich(f: ((String, RichLogger)) => Boolean): Option[(String, RichLogger)] =
    richLogger.find(f)

  trait Init {
    val logPrefix: String
    val isTraceExtraEnabled: Boolean
    val isTraceEnabled: Boolean
    val isDebugEnabled: Boolean
    val isInfoEnabled: Boolean
    val isWarnEnabled: Boolean
    val isErrorEnabled: Boolean
    val shutdownHook: Thread
    val richLoggerBuilder: (String) => RichLogger
    val flushLimit: Int
    val appenders: Seq[Appender]
  }
  class DefaultInit extends Init {
    val logPrefix = "@" // prefix for all adb logcat TAGs, everyone may change (but should not) it on his/her own risk
    val isTraceExtraEnabled = false
    val isTraceEnabled = true
    val isDebugEnabled = true
    val isInfoEnabled = true
    val isWarnEnabled = true
    val isErrorEnabled = true
    val shutdownHook = new Thread() { override def run() = deinit }
    val richLoggerBuilder = (name: String) => new RichLogger(OriginalLoggerFactory.getLogger(name))
    val flushLimit = 1000
    val appenders = Seq[Appender]()
    override def toString = "DefaultInit"
  }
  sealed trait Event
  object Event extends Publisher[Event] {
    override protected[Logging] def publish(event: Event) = try {
      super.publish(event)
    } catch {
      case e =>
        Logging.commonLogger.error(e.getMessage(), e)
    }
    case class Incoming(val record: Record) extends Event
    case class Outgoing(val record: Record) extends Event
    case class RegisterLogger(val logger: RichLogger) extends Event
  }
  object Where {
    val ALL = -1
    val HERE = -2
    val BEFORE = -3
  }
}
