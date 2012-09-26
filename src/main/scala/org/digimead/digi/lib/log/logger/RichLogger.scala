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

package org.digimead.digi.lib.log.logger

import scala.annotation.implicitNotFound

import org.digimead.digi.lib.log.Logging

@implicitNotFound(msg = "please define implicit RichLogger")
class RichLogger(val base: org.slf4j.Logger) {
  var traceEnabled: Option[Boolean] = None
  var debugEnabled: Option[Boolean] = None
  var infoEnabled: Option[Boolean] = None
  var warnEnabled: Option[Boolean] = None
  var errorEnabled: Option[Boolean] = None

  // fast look while development, highlight it in your IDE
  def ___gaze(msg: String) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    base.error("<<< " + msg + " >>>\n" + t.getStackTraceString)
  }
  // fast look while development, highlight it in your IDE
  def ___glance(msg: String) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    base.error("<<< " + msg + " >>>")
  }
  // error with stack trace
  def fatal(msg: String) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    base.error(msg, t)
  }
  def isTraceExtraEnabled(): Boolean = Logging.isTraceExtraEnabled
  /* @see org.slf4j.Logger#isTraceEnabled() */
  def isTraceEnabled(): Boolean = traceEnabled getOrElse Logging.isTraceEnabled
  def traceWhere(msg: String): Unit = if (isTraceEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, base.trace, base.trace)(-2)
    else
      base.trace(msg)
  def traceWhere(msg: String, stackLine: Int): Unit = if (isTraceEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, base.trace, base.trace)(stackLine)
    else
      base.trace(msg)

  /* @see org.slf4j.Logger#isDebugEnabled() */
  def isDebugEnabled(): Boolean = debugEnabled getOrElse Logging.isDebugEnabled
  def debugWhere(msg: String): Unit = if (isDebugEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, base.debug, base.debug)(-2)
    else
      base.debug(msg)
  def debugWhere(msg: String, stackLine: Int): Unit = if (isDebugEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, base.debug, base.debug)(stackLine)
    else
      base.debug(msg)

  /* @see org.slf4j.Logger#isInfoEnabled() */
  def isInfoEnabled: Boolean = infoEnabled getOrElse Logging.isInfoEnabled
  def infoWhere(msg: String): Unit = if (isInfoEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, base.info, base.info)(-2)
    else
      base.info(msg)
  def infoWhere(msg: String, stackLine: Int = 4): Unit = if (isInfoEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, base.info, base.info)(stackLine)
    else
      base.info(msg)

  /* @see org.slf4j.Logger#isWarnEnabled() */
  def isWarnEnabled: Boolean = warnEnabled getOrElse Logging.isWarnEnabled
  def warnWhere(msg: String): Unit = if (isWarnEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, base.warn, base.warn)(-2)
    else
      base.warn(msg)
  def warnWhere(msg: String, stackLine: Int = 4): Unit = if (isWarnEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, base.warn, base.warn)(stackLine)
    else
      base.warn(msg)

  /* @see org.slf4j.Logger#isErrorEnabled() */
  def isErrorEnabled: Boolean = errorEnabled getOrElse Logging.isErrorEnabled
  def errorWhere(msg: String): Unit = if (isErrorEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, base.error, base.error)(-2)
    else
      base.error(msg)
  def errorWhere(msg: String, stackLine: Int): Unit = if (isErrorEnabled)
    if (Logging.isTraceExtraEnabled)
      logWhere(msg, base.error, base.error)(stackLine)
    else
      base.error(msg)

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

object RichLogger {
  implicit def rich2slf4j(l: RichLogger): org.slf4j.Logger = l.base
}
