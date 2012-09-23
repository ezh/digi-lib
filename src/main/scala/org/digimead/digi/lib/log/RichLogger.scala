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
import scala.annotation.implicitNotFound
import org.slf4j.Marker
import org.slf4j.helpers.MarkerIgnoringBase

@implicitNotFound(msg = "please define implicit RichLogger")
class RichLogger(val loggerName: java.lang.String) extends MarkerIgnoringBase {
  override def getName(): String = loggerName
  // fast look while development, highlight it in your IDE
  def ___gaze(msg: String) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    error("<<< " + msg + " >>>\n" + t.getStackTraceString)
  }
  // fast look while development, highlight it in your IDE
  def ___glance(msg: String) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    error("<<< " + msg + " >>>")
  }
  // error with stack trace
  def fatal(msg: String) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    error(msg, t)
  }
  def isTraceExtraEnabled(): Boolean = Logging.isTraceExtraEnabled
  // trace
  override protected def isTraceEnabled(marker: Marker) = isTraceEnabled
  /* @see org.slf4j.Logger#isTraceEnabled() */
  def isTraceEnabled(): Boolean = Logging.isTraceEnabled
  def trace(msg: String) = if (Logging.isTraceEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName, msg))

  protected def trace(format: String, arg: Object) = if (Logging.isTraceEnabled) {
    val msg = format.format(arg)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName, msg))
  }
  protected def trace(format: String, arg1: Object, arg2: Object) = if (Logging.isTraceEnabled) {
    val msg = format.format(arg1, arg2)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName, msg))
  }
  def trace(format: String, argArray: AnyRef*): Unit = if (Logging.isTraceEnabled) {
    val msg = format.format(argArray: _*)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName, msg))
  }
  def trace(msg: String, t: Throwable) = if (Logging.isTraceEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName, msg, Some(t)))
  def traceWhere(msg: String): Unit = if (Logging.isTraceEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, trace, trace)(-2)
    else
      trace(msg)
  def traceWhere(msg: String, stackLine: Int): Unit = if (Logging.isTraceEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, trace, trace)(stackLine)
    else
      trace(msg)

  // debug
  override protected def isDebugEnabled(marker: Marker) = isDebugEnabled
  /* @see org.slf4j.Logger#isDebugEnabled() */
  def isDebugEnabled(): Boolean = Logging.isDebugEnabled
  def debug(msg: String) = if (Logging.isDebugEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName, msg))
  protected def debug(format: String, arg: Object) = if (Logging.isDebugEnabled) {
    val msg = format.format(arg)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName, msg))
  }
  protected def debug(format: String, arg1: Object, arg2: Object) = if (Logging.isDebugEnabled) {
    val msg = format.format(arg1, arg2)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName, msg))
  }
  def debug(format: String, argArray: AnyRef*): Unit = if (Logging.isDebugEnabled) {
    val msg = format.format(argArray: _*)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName, msg))
  }
  def debug(msg: String, t: Throwable) = if (Logging.isDebugEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName, msg, Some(t)))
  def debugWhere(msg: String): Unit = if (Logging.isDebugEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, debug, debug)(-2)
    else
      debug(msg)
  def debugWhere(msg: String, stackLine: Int): Unit = if (Logging.isDebugEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, debug, debug)(stackLine)
    else
      debug(msg)

  // info
  override protected def isInfoEnabled(marker: Marker) = isInfoEnabled
  /* @see org.slf4j.Logger#isInfoEnabled() */
  def isInfoEnabled: Boolean = Logging.isInfoEnabled
  def info(msg: String) = if (Logging.isInfoEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName, msg))
  protected def info(format: String, arg: Object) = if (Logging.isInfoEnabled) {
    val msg = format.format(arg)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName, msg))
  }
  protected def info(format: String, arg1: Object, arg2: Object) = if (Logging.isInfoEnabled) {
    val msg = format.format(arg1, arg2)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName, msg))
  }
  def info(format: String, argArray: AnyRef*): Unit = if (Logging.isInfoEnabled) {
    val msg = format.format(argArray: _*)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName, msg))
  }
  def info(msg: String, t: Throwable) = if (Logging.isInfoEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName, msg, Some(t)))
  def infoWhere(msg: String): Unit = if (Logging.isInfoEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, info, info)(-2)
    else
      info(msg)
  def infoWhere(msg: String, stackLine: Int = 4): Unit = if (Logging.isInfoEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, info, info)(stackLine)
    else
      info(msg)

  // warn
  override protected def isWarnEnabled(marker: Marker) = isWarnEnabled
  /* @see org.slf4j.Logger#isWarnEnabled() */
  def isWarnEnabled: Boolean = Logging.isWarnEnabled
  def warn(msg: String) = if (Logging.isWarnEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName, msg))
  protected def warn(format: String, arg: Object) = if (Logging.isWarnEnabled) {
    val msg = format.format(arg)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName, msg))
  }
  protected def warn(format: String, arg1: Object, arg2: Object) = if (Logging.isWarnEnabled) {
    val msg = format.format(arg1, arg2)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName, msg))
  }
  def warn(format: String, argArray: AnyRef*): Unit = if (Logging.isWarnEnabled) {
    val msg = format.format(argArray: _*)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName, msg))
  }
  def warn(msg: String, t: Throwable) = if (Logging.isWarnEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName, msg, Some(t)))
  def warnWhere(msg: String): Unit = if (Logging.isWarnEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, warn, warn)(-2)
    else
      warn(msg)
  def warnWhere(msg: String, stackLine: Int = 4): Unit = if (Logging.isWarnEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, warn, warn)(stackLine)
    else
      warn(msg)

  // error
  override protected def isErrorEnabled(marker: Marker) = isErrorEnabled
  /* @see org.slf4j.Logger#isErrorEnabled() */
  def isErrorEnabled: Boolean = Logging.isErrorEnabled
  def error(msg: String) = if (Logging.isErrorEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName, msg))
  protected def error(format: String, arg: Object) = if (Logging.isErrorEnabled) {
    val msg = format.format(arg)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName, msg))
  }
  protected def error(format: String, arg1: Object, arg2: Object) = if (Logging.isErrorEnabled) {
    val msg = format.format(arg1, arg2)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName, msg))
  }
  def error(format: String, argArray: AnyRef*): Unit = if (Logging.isErrorEnabled) {
    val msg = format.format(argArray: _*)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName, msg))
  }
  // always enabled
  def error(msg: String, t: Throwable) {
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName, msg, Some(t)))
  }
  def errorWhere(msg: String): Unit = if (Logging.isErrorEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, error, error)(-2)
    else
      error(msg)
  def errorWhere(msg: String, stackLine: Int): Unit = if (Logging.isErrorEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, error, error)(stackLine)
    else
      error(msg)

  private def logWhere(msg: String, f1: (String, Throwable) => Unit, f2: (String => Unit))(stackLine: Int) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    if (stackLine == -1) { // ALL
      if (isTraceEnabled) {
        f1(msg, t)
      } else { // HERE
        val trace = t.getStackTrace
        val skip = trace.takeWhile(_.getFileName == "RichLogger.scala").size
        f2(msg + " at " + trace.take(skip + (-2 * -1) - 1).last)
      }
    } else if (stackLine <= -2) {
      val trace = t.getStackTrace
      val skip = trace.takeWhile(_.getFileName == "RichLogger.scala").size
      f2(msg + " at " + trace.take(skip + (stackLine * -1) - 1).last)
    } else
      f2(msg + " at " + t.getStackTrace.take(stackLine + 1).last)
  }
}
