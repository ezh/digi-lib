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
import scala.annotation.implicitNotFound
import scala.annotation.tailrec
import scala.collection.immutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.Publisher
import scala.collection.mutable.SynchronizedMap
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import org.digimead.digi.lib.DependencyInjection
import org.digimead.digi.lib.log.appender.Appender
import org.digimead.digi.lib.log.logger.RichLogger
import org.digimead.digi.lib.log.logger.RichLogger.rich2slf4j
import org.slf4j.LoggerFactory

import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.Injectable

import scala.language.implicitConversions

class Logging(implicit val bindingModule: BindingModule) extends Injectable {
  val record = inject[Record]
  val builder = inject[(String) => RichLogger]("Log.Builder")
  /** prefix for all adb logcat TAGs, everyone may change (but should not) it on his/her own risk */
  val logPrefix = injectOptional[String]("Log.Buffered") getOrElse "@"
  val isTraceWhereEnabled = injectOptional[Boolean]("Log.TraceWhereEnabled") getOrElse false
  val bufferedThread = inject[Option[Logging.BufferedLogThread]]("Log.BufferedThread")
  val bufferedFlushLimit = injectOptional[Int]("Log.BufferedFlushLimit") getOrElse 1000
  val bufferedAppender = if (bufferedThread.nonEmpty)
    inject[HashSet[Appender]]("Log.BufferedAppenders")
  else
    HashSet[Appender]()
  val shutdownHook = injectOptional[() => Any]("Log.ShutdownHook")
  val richLogger = new HashMap[String, RichLogger]() with SynchronizedMap[String, RichLogger]
  lazy val commonLogger: RichLogger = {
    val name = if (bufferedThread.nonEmpty) "@~*~*~*~*" else getClass.getName()
    val logger = new RichLogger(LoggerFactory.getLogger(name), isTraceWhereEnabled)
    richLogger(name) = logger
    logger
  }
  val bufferedQueue = new ConcurrentLinkedQueue[Record.Message]
  private val bufferedSlice = new Array[Record.Message](bufferedFlushLimit)

  def init() {
    Logging.addToLog(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName,
      if (bufferedThread.nonEmpty)
        "initialize logging with buffered slf4j logger and " + this.toString
      else
        "initialize logging with direct slf4j logger and " + this.toString)
    bufferedAppender.foreach {
      appender =>
        Logging.addToLog(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName, "initialize appender " + appender)
        appender.init()
    }
    bufferedThread.foreach(_.init)
  }

  def deinit() {
    bufferedThread.foreach(_.deinit)
    Logging.addToLog(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName, "deinitialize " + this.toString)
    flush(0)
    bufferedQueue.clear()
    bufferedQueue.synchronized { bufferedQueue.notifyAll() }
    bufferedAppender.foreach {
      appender =>
        Logging.addToLog(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName, "deinitialize appender " + appender)
        flush(0)
        appender.flush()
        appender.deinit()
    }
  }
  def offer(record: Record.Message) = bufferedQueue.synchronized {
    bufferedQueue.offer(record)
    bufferedQueue.notifyAll
    Logging.Event.publish(new Logging.Event.Incoming(record))
  }
  def flush(timeout: Int): Int = synchronized {
    if (bufferedAppender.isEmpty)
      return -1
    val flushed = flushQueue(timeout)
    bufferedAppender.foreach(_.flush)
    flushed
  }
  private def flushQueue(timeout: Int): Int = flushQueue(Int.MaxValue, timeout)
  @tailrec
  final private[log] def flushQueue(n: Int, timeout: Int, accumulator: Int = 0): Int = {
    var records = 0
    bufferedSlice.synchronized {
      val limit = if (n <= bufferedFlushLimit) (n - accumulator) else bufferedFlushLimit
      while (records < limit && !bufferedQueue.isEmpty()) {
        bufferedSlice(records) = bufferedQueue.poll().asInstanceOf[Record.Message]
        Logging.Event.publish(new Logging.Event.Outgoing(bufferedSlice(records)))
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

object Logging extends DependencyInjection.PersistentInjectable {
  implicit def Logging2implementation(l: Logging.type): Logging = inner
  implicit def bindingModule = DependencyInjection()
  private val loggingObjectName = getClass.getName
  private val loggableClassName = classOf[Loggable].getName
  /** Logging implementation DI cache */
  @volatile private var implementation = inject[Logging]
  Runtime.getRuntime().addShutdownHook(new Thread {
    override def run = if (DependencyInjection.get.nonEmpty) Logging.injectOptional[Logging].foreach(_.shutdownHook.foreach(_()))
  })

  def addToLog(date: Date, tid: Long, level: Record.Level, tag: String, message: String): Unit =
    addToLog(date, tid, level, tag, message, None)
  def addToLog(date: Date, tid: Long, level: Record.Level, tag: String, message: String, throwable: Option[Throwable]): Unit =
    addToLog(date, tid, level, tag, message, throwable, inner.record.pid)
  def addToLog(date: Date, tid: Long, level: Record.Level, tag: String, message: String, throwable: Option[Throwable], pid: Int): Unit = {
    val implementation = inner()
    if (implementation.bufferedThread.nonEmpty)
      implementation.offer(implementation.record.builder(date, tid, level, tag, message, throwable, pid))
    else
      level match {
        case Record.Level.Trace => implementation.commonLogger.trace(message)
        case Record.Level.Debug => implementation.commonLogger.debug(message)
        case Record.Level.Info => implementation.commonLogger.info(message)
        case Record.Level.Warn => implementation.commonLogger.warn(message)
        case Record.Level.Error => implementation.commonLogger.error(message)
      }
  }
  /**
   * transform clazz to filename and return logger name
   */
  def getLogger(clazz: Class[_]): RichLogger = {
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
    getLogger(namePrefix + name1stPart + "." + name2ndPart + nameSuffix)
  }
  def getLogger(name: String): RichLogger = {
    val implementation = inner
    implementation.richLogger.get(name) match {
      case Some(logger) => logger
      case None =>
        val logger = implementation.builder(name)
        implementation.richLogger(name) = logger
        Event.publish(new Event.RegisterLogger(logger))
        logger
    }
  }

  /*
   * dependency injection
   */
  def inner() = implementation
  override def injectionAfter(newModule: BindingModule) {
    implementation = inject[Logging]
    inner.init()
  }
  override def injectionOnClear(oldModule: BindingModule) {
    inner.deinit()
  }

  abstract class BufferedLogThread extends Thread("Generic buffered logger for " + Logging.getClass.getName) {
    def init(): Unit
    def threadSuspend(): Unit
    def threadResume(): Unit
    def deinit(): Unit
  }
  sealed trait Event
  object Event extends Publisher[Event] {
    override protected[Logging] def publish(event: Event) = try {
      super.publish(event)
    } catch {
      // This catches all Throwables because we want to record exception to log file
      case e: Throwable =>
        inner.commonLogger.error(e.getMessage(), e)
    }
    case class Incoming(val record: Record.Message) extends Event
    case class Outgoing(val record: Record.Message) extends Event
    case class RegisterLogger(val logger: RichLogger) extends Event
  }
  object Where {
    val ALL = -1
    val HERE = -2
    val BEFORE = -3
  }
}
