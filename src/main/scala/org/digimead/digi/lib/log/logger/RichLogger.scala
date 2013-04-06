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

package org.digimead.digi.lib.log.logger

import scala.annotation.implicitNotFound

import org.digimead.digi.lib.log.Logging

import scala.language.implicitConversions

@implicitNotFound(msg = "please define implicit RichLogger")
class RichLogger private[log] (val base: org.slf4j.Logger, val isTraceWhereEnabled: Boolean) {
  protected lazy val traceFuncS: (String => Unit) = (base.isTraceEnabled, isTraceWhereEnabled) match {
    case (true, true) => (msg: String) => logWhere(msg, base.trace, base.trace)(-2)
    case (true, false) => (msg: String) => base.trace(msg)
    case (false, _) => (msg: String) => {}
  }
  protected lazy val traceFuncSI: ((String, Int) => Unit) = (base.isTraceEnabled, isTraceWhereEnabled) match {
    case (true, true) => (msg: String, stackLine: Int) => logWhere(msg, base.trace, base.trace)(stackLine)
    case (true, false) => (msg: String, stackLine: Int) => base.trace(msg)
    case (false, _) => (msg: String, stackLine: Int) => {}
  }

  protected lazy val debugFuncS: (String => Unit) = (base.isTraceEnabled, isTraceWhereEnabled) match {
    case (true, true) => (msg: String) => logWhere(msg, base.debug, base.debug)(-2)
    case (true, false) => (msg: String) => base.debug(msg)
    case (false, _) => (msg: String) => {}
  }
  protected lazy val debugFuncSI: ((String, Int) => Unit) = (base.isTraceEnabled, isTraceWhereEnabled) match {
    case (true, true) => (msg: String, stackLine: Int) => logWhere(msg, base.debug, base.debug)(stackLine)
    case (true, false) => (msg: String, stackLine: Int) => base.debug(msg)
    case (false, _) => (msg: String, stackLine: Int) => {}
  }

  protected lazy val infoFuncS: (String => Unit) = (base.isTraceEnabled, isTraceWhereEnabled) match {
    case (true, true) => (msg: String) => logWhere(msg, base.info, base.info)(-2)
    case (true, false) => (msg: String) => base.info(msg)
    case (false, _) => (msg: String) => {}
  }
  protected lazy val infoFuncSI: ((String, Int) => Unit) = (base.isTraceEnabled, isTraceWhereEnabled) match {
    case (true, true) => (msg: String, stackLine: Int) => logWhere(msg, base.info, base.info)(stackLine)
    case (true, false) => (msg: String, stackLine: Int) => base.info(msg)
    case (false, _) => (msg: String, stackLine: Int) => {}
  }

  protected lazy val warnFuncS: (String => Unit) = (base.isTraceEnabled, isTraceWhereEnabled) match {
    case (true, true) => (msg: String) => logWhere(msg, base.warn, base.warn)(-2)
    case (true, false) => (msg: String) => base.warn(msg)
    case (false, _) => (msg: String) => {}
  }
  protected lazy val warnFuncSI: ((String, Int) => Unit) = (base.isTraceEnabled, isTraceWhereEnabled) match {
    case (true, true) => (msg: String, stackLine: Int) => logWhere(msg, base.warn, base.warn)(stackLine)
    case (true, false) => (msg: String, stackLine: Int) => base.warn(msg)
    case (false, _) => (msg: String, stackLine: Int) => {}
  }

  protected lazy val errorFuncS: (String => Unit) = (base.isTraceEnabled, isTraceWhereEnabled) match {
    case (true, true) => (msg: String) => logWhere(msg, base.error, base.error)(-2)
    case (true, false) => (msg: String) => base.error(msg)
    case (false, _) => (msg: String) => {}
  }
  protected lazy val errorFuncSI: ((String, Int) => Unit) = (base.isTraceEnabled, isTraceWhereEnabled) match {
    case (true, true) => (msg: String, stackLine: Int) => logWhere(msg, base.error, base.error)(stackLine)
    case (true, false) => (msg: String, stackLine: Int) => base.error(msg)
    case (false, _) => (msg: String, stackLine: Int) => {}
  }
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

  def traceWhere(msg: String): Unit = traceFuncS(msg)
  def traceWhere(msg: String, stackLine: Int): Unit = traceFuncSI(msg, stackLine)

  def debugWhere(msg: String): Unit = debugFuncS(msg)
  def debugWhere(msg: String, stackLine: Int): Unit = debugFuncSI(msg, stackLine)

  def infoWhere(msg: String): Unit = infoFuncS(msg)
  def infoWhere(msg: String, stackLine: Int): Unit = infoFuncSI(msg, stackLine)

  def warnWhere(msg: String): Unit = warnFuncS(msg)
  def warnWhere(msg: String, stackLine: Int): Unit = warnFuncSI(msg, stackLine)

  def errorWhere(msg: String): Unit = errorFuncS(msg)
  def errorWhere(msg: String, stackLine: Int): Unit = errorFuncS(msg)

  protected def logWhere(msg: String, f1: (String, Throwable) => Unit, f2: (String => Unit))(stackLine: Int) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    if (stackLine == -1) { // ALL
      if (base.isTraceEnabled) {
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
