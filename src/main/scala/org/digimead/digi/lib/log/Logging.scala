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

import java.util.Date
import java.util.concurrent.ConcurrentLinkedQueue

import scala.Array.canBuildFrom
import scala.Option.option2Iterable
import scala.annotation.implicitNotFound
import scala.annotation.tailrec
import scala.collection.immutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.Publisher
import scala.collection.mutable.SynchronizedMap

import org.digimead.digi.lib.DependencyInjection
import org.digimead.digi.lib.DependencyInjection.PersistentInjectable
import org.digimead.digi.lib.log.appender.Appender
import org.digimead.digi.lib.log.logger.RichLogger
import org.digimead.digi.lib.log.logger.RichLogger.rich2slf4j
import org.scala_tools.subcut.inject.BindingModule
import org.scala_tools.subcut.inject.{ Injectable => SubCutInjectable }
import org.slf4j.LoggerFactory

class Logging(implicit val bindingModule: BindingModule) extends SubCutInjectable {
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
    try {
      Logging.Event.publish(new Logging.Event.Incoming(record))
    } catch {
      case e =>
        bufferedQueue.offer(this.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Debug,
          commonLogger.getName, e.getMessage match {
            case null => ""
            case message => message
          }, Some(e), this.record.pid))
    }
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
        try {
          Logging.Event.publish(new Logging.Event.Outgoing(bufferedSlice(records)))
        } catch {
          case e =>
            Logging.addToLog(new Date(), Thread.currentThread.getId, Record.Level.Debug, commonLogger.getName,
              e.getMessage match {
                case null => ""
                case message => message
              }, Some(e))
        }
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

object Logging extends PersistentInjectable {
  implicit def instance2Logging(l: Logging.type): Logging = instance
  implicit def bindingModule = DependencyInjection()
  @volatile private var instance = inject[Logging]
  Runtime.getRuntime().addShutdownHook(new Thread { override def run = Logging.instance.shutdownHook.foreach(_()) })
  instance.init()

  def addToLog(date: Date, tid: Long, level: Record.Level, tag: String, message: String): Unit =
    addToLog(date, tid, level, tag, message, None)
  def addToLog(date: Date, tid: Long, level: Record.Level, tag: String, message: String, throwable: Option[Throwable]): Unit =
    addToLog(date, tid, level, tag, message, throwable, instance.record.pid)
  def addToLog(date: Date, tid: Long, level: Record.Level, tag: String, message: String, throwable: Option[Throwable], pid: Int): Unit =
    if (instance.bufferedThread.nonEmpty)
      instance.offer(instance.record.builder(date, tid, level, tag, message, throwable, pid))
    else
      level match {
        case Record.Level.Trace => instance.commonLogger.trace(message)
        case Record.Level.Debug => instance.commonLogger.debug(message)
        case Record.Level.Info => instance.commonLogger.info(message)
        case Record.Level.Warn => instance.commonLogger.warn(message)
        case Record.Level.Error => instance.commonLogger.error(message)
      }
  def getLogger(clazz: Class[_]): RichLogger = {
    val stackArray = Thread.currentThread.getStackTrace().dropWhile(_.getClassName != getClass.getName)
    val stack = if (stackArray(1).getFileName != stackArray(0).getFileName)
      stackArray(1) else stackArray(2)
    val fileRaw = stack.getFileName.split("""\.""")
    val fileParsed = if (fileRaw.length > 1)
      fileRaw.dropRight(1).mkString(".")
    else
      fileRaw.head
    val loggerName = if (clazz.getClass().toString.last == '$') // add object mark to file name
      instance.logPrefix + clazz.getClass.getPackage.getName.split("""\.""").last + "." + fileParsed + "$"
    else
      instance.logPrefix + clazz.getClass.getPackage.getName.split("""\.""").last + "." + fileParsed
    getLogger(loggerName)
  }
  def getLogger(name: String): RichLogger =
    instance.richLogger.get(name) match {
      case Some(logger) => logger
      case None =>
        val logger = instance.builder(name)
        instance.richLogger(name) = logger
        Event.publish(new Event.RegisterLogger(logger))
        logger

    }
  def reloadInjection() = synchronized {
    instance.deinit()
    instance = inject[Logging]
    instance.init
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
      case e =>
        instance.commonLogger.error(e.getMessage(), e)
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
