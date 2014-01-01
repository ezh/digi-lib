/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012-2013 Alexey Aksenov ezh@ezh.msk.ru
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

import java.util.Date
import java.util.concurrent.ConcurrentLinkedQueue

import scala.Array.canBuildFrom
import scala.annotation.tailrec
import scala.collection.immutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.Publisher
import scala.collection.mutable.SynchronizedMap
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import org.digimead.digi.lib.api.DependencyInjection
import org.digimead.digi.lib.log.api.Loggable
import org.digimead.digi.lib.log.api.RichLogger
import org.digimead.digi.lib.log.api.Appender
import org.slf4j.LoggerFactory

import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.Injectable

import language.implicitConversions

class Logging(implicit val bindingModule: BindingModule) extends Injectable {
  val record = inject[Record]
  val builder = inject[(String, Class[_]) => RichLogger]("Log.Builder")
  /** prefix for all adb logcat TAGs, everyone may change (but should not) it on his/her own risk */
  val logPrefix = injectOptional[String]("Log.Buffered") getOrElse "@"
  val isWhereEnabled = injectOptional[Boolean]("Log.TraceWhereEnabled") getOrElse false
  val bufferedThread = inject[Option[Logging.BufferedLogThread]]("Log.BufferedThread")
  val bufferedFlushLimit = injectOptional[Int]("Log.BufferedFlushLimit") getOrElse 1000
  val bufferedAppender = {
    val appenders = injectOptional[HashSet[Appender]]("Log.BufferedAppenders") getOrElse HashSet[Appender]()
    if (appenders.nonEmpty)
      assert(bufferedThread.nonEmpty, "DI Log.BufferedThread is lost in space")
    appenders
  }
  val shutdownHook = injectOptional[() => Any]("Log.ShutdownHook")
  val richLogger = new HashMap[String, RichLogger]() with SynchronizedMap[String, RichLogger]
  lazy val commonLogger: RichLogger = {
    val name = if (bufferedThread.nonEmpty) "@~*~*~*~*" else getClass.getName()
    val logger = new org.digimead.digi.lib.log.logger.RichLogger(LoggerFactory.getLogger(name), isWhereEnabled)
    richLogger(name) = logger
    logger
  }
  val bufferedQueue = new ConcurrentLinkedQueue[api.Message]
  private val bufferedSlice = new Array[api.Message](bufferedFlushLimit)

  /** Deinitialize logging. */
  def deinit() {
    bufferedThread.foreach(_.deinit)
    Logging.addToLog(new Date(), Thread.currentThread.getId, api.Level.Debug, commonLogger.getName, getClass, s"Deinitialize ${this.toString}.")
    flush(0)
    bufferedQueue.clear()
    bufferedQueue.synchronized { bufferedQueue.notifyAll() }
    bufferedAppender.foreach {
      appender =>
        Logging.addToLog(new Date(), Thread.currentThread.getId, api.Level.Debug, commonLogger.getName, getClass, s"Deinitialize appender ${appender}.")
        flush(0)
        appender.flush()
        appender.deinit()
    }
  }
  /** Flush buffered appenders. */
  def flush(timeout: Int): Int = synchronized {
    if (bufferedAppender.isEmpty)
      return -1
    val flushed = flushQueue(timeout)
    bufferedAppender.foreach(_.flush)
    flushed
  }
  /** Initialize logging. */
  def init() {
    Logging.addToLog(new Date(), Thread.currentThread.getId, api.Level.Debug, commonLogger.getName, getClass,
      if (bufferedThread.nonEmpty)
        s"Initialize logging with buffered slf4j logger and ${this.toString}."
      else
        s"Initialize logging with direct slf4j logger and ${this.toString}.")
    bufferedAppender.foreach {
      appender =>
        Logging.addToLog(new Date(), Thread.currentThread.getId, api.Level.Debug, commonLogger.getName, getClass, s"Initialize appender $appender.")
        appender.init()
    }
    bufferedThread.foreach(_.init)
  }
  /** Add record to buffered queue. */
  def offer(record: api.Message) = bufferedQueue.synchronized {
    bufferedQueue.offer(record)
    bufferedQueue.notifyAll
  }
  /** Rotate log files. */
  def rotate() {
    flush(1000) // flush with maximum timeout 1s
    bufferedAppender.foreach(_.flush)
    bufferedAppender.foreach(_.deinit)
    bufferedAppender.foreach(_.init)
  }

  protected def flushQueue(timeout: Int): Int = flushQueue(Int.MaxValue, timeout)
  @tailrec
  final private[log] def flushQueue(n: Int, timeout: Int, accumulator: Int = 0): Int = {
    var records = 0
    bufferedSlice.synchronized {
      val limit = if (n <= bufferedFlushLimit) (n - accumulator) else bufferedFlushLimit
      while (records < limit && !bufferedQueue.isEmpty()) {
        bufferedSlice(records) = bufferedQueue.poll().asInstanceOf[api.Message]
        api.Event.publish(new api.Event.Outgoing(bufferedSlice(records)))
        records += 1
      }
      bufferedAppender.foreach(_(bufferedSlice.take(records)))
    }
    if (records == bufferedFlushLimit) {
      if (records == n)
        return accumulator + records
    } else
      return accumulator + records
    Thread.sleep(timeout)
    flushQueue(n, timeout, accumulator + records)
  }
  def dump() = ("queue size: " + bufferedQueue.size + ", buffered appenders: " + bufferedAppender.mkString(",") +
    " thread: " + bufferedThread) +: bufferedQueue.toArray.map(_.toString)
  override def toString() = "default Logging implementation"
}

object Logging {
  implicit def Logging2implementation(l: Logging.type): Logging = inner
  private val loggingObjectName = getClass.getName
  private val loggableClassName = classOf[org.digimead.digi.lib.log.api.Loggable].getName

  /**
   * Create api.Message implementation with api.Message.MessageBuilder
   *
   * @param date      log timestamp
   * @param tid       log origin Thread ID
   * @param level     log message level
   * @param tag       external tag representation (class name) suitable for end user
   * @param tagClass  internal(full) tag representation suitable for message filtering
   * @param message   log message
   */
  def addToLog(date: Date, tid: Long, level: api.Level, tag: String, tagClass: Class[_], message: String): Unit =
    addToLog(date, tid, level, tag, tagClass, message, None)
  /**
   * Create Record.Message implementation with Record.MessageBuilder
   *
   * @param date      log timestamp
   * @param tid       log origin Thread ID
   * @param level     log message level
   * @param tag       external tag representation (class name) suitable for end user
   * @param tagClass  internal(full) tag representation suitable for message filtering
   * @param message   log message
   * @param throwable log throwable if any
   */
  def addToLog(date: Date, tid: Long, level: api.Level, tag: String, tagClass: Class[_], message: String, throwable: Option[Throwable]): Unit =
    addToLog(date, tid, level, tag, tagClass, message, throwable, inner.record.pid)
  /**
   * Create Record.Message implementation with Record.MessageBuilder
   *
   * @param date     log timestamp
   * @param tid      log origin Thread ID
   * @param level    log message level
   * @param tag      external tag representation (class name) suitable for end user
   * @param tagClass internal(full) tag representation suitable for message filtering
   * @param message  log message
   * @param pid      log Process ID. Very handy in distributed environment.
   */
  def addToLog(date: Date, tid: Long, level: api.Level, tag: String, tagClass: Class[_], message: String, throwable: Option[Throwable], pid: Int): Unit = {
    val implementation = inner()
    if (implementation.bufferedThread.nonEmpty)
      implementation.offer(implementation.record.builder(date, tid, level, tag, tagClass, message, throwable, pid))
    else
      level match {
        case api.Level.Trace => implementation.commonLogger.trace(message)
        case api.Level.Debug => implementation.commonLogger.debug(message)
        case api.Level.Info => implementation.commonLogger.info(message)
        case api.Level.Warn => implementation.commonLogger.warn(message)
        case api.Level.Error => implementation.commonLogger.error(message)
      }
  }
  /**
   * transform clazz to filename and return logger name
   */
  // Synchronized per class.
  def getLogger(clazz: Class[_]): RichLogger = clazz.synchronized {
    val stackArray = Thread.currentThread.getStackTrace().dropWhile(_.getClassName != getClass.getName)
    // current class element
    var thisMethodElement: Option[StackTraceElement] = None
    // client class element
    var thatMethodElement: Option[StackTraceElement] = None
    breakable {
      for (i <- 0 until stackArray.size) {
        thisMethodElement match {
          case Some(element) if stackArray(i).getFileName != element.getFileName &&
            !stackArray(i).getClassName.startsWith(loggableClassName) =>
            // client method element found
            thatMethodElement = Some(stackArray(i))
            break
          case Some(element) =>
          // skip element before thatMethodElement
          case None if stackArray(i).getClassName != loggingObjectName =>
            // current method element found
            thisMethodElement = Some(stackArray(i))
          case None =>
          // skip element before thisMethodElement
        }
      }
    }
    val namePrefix = inner.logPrefix
    val name1stPart = clazz.getPackage.getName.split("""\.""").last
    val name2ndPart = thatMethodElement match {
      case Some(element) =>
        val fileRaw = element.getFileName.split("""\.""")
        if (fileRaw.length > 1) fileRaw.dropRight(1).mkString(".") else fileRaw.head
      case None =>
        clazz.getName.split("""[\.$]""").last
    }
    val nameSuffix = if (clazz.getClass().toString.last == '$') "$" else ""
    getLogger(namePrefix + name1stPart + "." + name2ndPart + nameSuffix, clazz)
  }
  def getLogger(name: String, tagClass: Class[_] = null): RichLogger = {
    val implementation = inner
    implementation.richLogger.get(name) match {
      case Some(logger) => logger
      case None =>
        val logger = implementation.builder(name, Option(tagClass).getOrElse(try {
          Class.forName(name)
        } catch {
          case e: Throwable =>
            classOf[AnyRef]
        }))
        implementation.richLogger(name) = logger
        api.Event.publish(new api.Event.RegisterLogger(logger))
        logger
    }
  }
  def inner() = DI.implementation
  def shutdownHook() = DI.shutdownHook

  abstract class BufferedLogThread extends Thread(s"Generic buffered logger for ${Logging.getClass.getName}") {
    def init(): Unit
    def threadSuspend(): Unit
    def threadResume(): Unit
    def deinit(): Unit
  }
  /**
   * Dependency injection routines
   */
  private object DI extends DependencyInjection.PersistentInjectable {
    /** Logging implementation */
    lazy val implementation = inject[Logging]
    /** User defined shutdown hook */
    lazy val shutdownHook = injectOptional[() => Any]("Log.ShutdownHook")
  }
}
