/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012-2015 Alexey Aksenov ezh@ezh.msk.ru
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

import org.digimead.digi.lib.log.api.{ XEvent, XLevel, XRichLogger }
import org.slf4j.Marker
import org.slf4j.helpers.MessageFormatter
import scala.annotation.implicitNotFound
import scala.language.reflectiveCalls

/** Implementation of org.digimead.digi.lib.log.api.RichLogger */
@implicitNotFound(msg = "please define implicit RichLogger")
class RichLogger private[log] (val base: org.slf4j.Logger, val isWhereEnabled: Boolean) extends XRichLogger {
  assert(base != null, "Base logger must not be null.")

  protected lazy val traceFuncS: (String ⇒ Unit) = (isTraceEnabled, isWhereEnabled) match {
    case (true, true) ⇒ (msg: String) ⇒ logWhere(msg, trace, trace)(-2)
    case (true, false) ⇒ (msg: String) ⇒ trace(msg)
    case (false, _) ⇒ (msg: String) ⇒ {}
  }
  protected lazy val traceFuncSI: ((String, Int) ⇒ Unit) = (isTraceEnabled, isWhereEnabled) match {
    case (true, true) ⇒ (msg: String, stackLine: Int) ⇒ logWhere(msg, trace, trace)(stackLine)
    case (true, false) ⇒ (msg: String, stackLine: Int) ⇒ trace(msg)
    case (false, _) ⇒ (msg: String, stackLine: Int) ⇒ {}
  }

  protected lazy val debugFuncS: (String ⇒ Unit) = (isDebugEnabled, isWhereEnabled) match {
    case (true, true) ⇒ (msg: String) ⇒ logWhere(msg, debug, debug)(-2)
    case (true, false) ⇒ (msg: String) ⇒ debug(msg)
    case (false, _) ⇒ (msg: String) ⇒ {}
  }
  protected lazy val debugFuncSI: ((String, Int) ⇒ Unit) = (isDebugEnabled, isWhereEnabled) match {
    case (true, true) ⇒ (msg: String, stackLine: Int) ⇒ logWhere(msg, debug, debug)(stackLine)
    case (true, false) ⇒ (msg: String, stackLine: Int) ⇒ debug(msg)
    case (false, _) ⇒ (msg: String, stackLine: Int) ⇒ {}
  }

  protected lazy val infoFuncS: (String ⇒ Unit) = (isInfoEnabled, isWhereEnabled) match {
    case (true, true) ⇒ (msg: String) ⇒ logWhere(msg, info, info)(-2)
    case (true, false) ⇒ (msg: String) ⇒ info(msg)
    case (false, _) ⇒ (msg: String) ⇒ {}
  }
  protected lazy val infoFuncSI: ((String, Int) ⇒ Unit) = (isInfoEnabled, isWhereEnabled) match {
    case (true, true) ⇒ (msg: String, stackLine: Int) ⇒ logWhere(msg, info, info)(stackLine)
    case (true, false) ⇒ (msg: String, stackLine: Int) ⇒ info(msg)
    case (false, _) ⇒ (msg: String, stackLine: Int) ⇒ {}
  }

  protected lazy val warnFuncS: (String ⇒ Unit) = (base.isTraceEnabled, isWhereEnabled) match {
    case (true, true) ⇒ (msg: String) ⇒ logWhere(msg, base.warn, base.warn)(-2)
    case (true, false) ⇒ (msg: String) ⇒ {
      base.warn(msg)
    }
    case (false, _) ⇒ (msg: String) ⇒ {}
  }
  protected lazy val warnFuncSI: ((String, Int) ⇒ Unit) = (base.isTraceEnabled, isWhereEnabled) match {
    case (true, true) ⇒ (msg: String, stackLine: Int) ⇒ logWhere(msg, base.warn, base.warn)(stackLine)
    case (true, false) ⇒ (msg: String, stackLine: Int) ⇒ {
      base.warn(msg)
    }
    case (false, _) ⇒ (msg: String, stackLine: Int) ⇒ {}
  }

  protected lazy val errorFuncS: (String ⇒ Unit) = (base.isTraceEnabled, isWhereEnabled) match {
    case (true, true) ⇒ (msg: String) ⇒ logWhere(msg, base.error, base.error)(-2)
    case (true, false) ⇒ (msg: String) ⇒ {
      base.error(msg)
    }
    case (false, _) ⇒ (msg: String) ⇒ {}
  }
  protected lazy val errorFuncSI: ((String, Int) ⇒ Unit) = (base.isTraceEnabled, isWhereEnabled) match {
    case (true, true) ⇒ (msg: String, stackLine: Int) ⇒ logWhere(msg, base.error, base.error)(stackLine)
    case (true, false) ⇒ (msg: String, stackLine: Int) ⇒ {
      base.error(msg)
    }
    case (false, _) ⇒ (msg: String, stackLine: Int) ⇒ {}
  }
  /** Fast look while development, highlight it in your IDE */
  def ___gaze(msg: String) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    base.error("<<< " + msg + " >>>\n" + t.getStackTrace.mkString("\n"))
  }
  /** Fast look while development, highlight it in your IDE */
  def ___glance(msg: String) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    base.error("<<< " + msg + " >>>")
  }
  /** Error with stack trace. */
  def fatal(msg: String) {
    val t = new Throwable(msg)
    t.fillInStackTrace()
    base.error(msg, t)
  }
  /** Error with stack trace and external exception. */
  def fatal(msg: String, tExt: Throwable) {
    val t = new Throwable(msg, tExt)
    t.fillInStackTrace()
    base.error(msg, t)
  }
  /**
   * Return the name of this <code>Logger</code> instance.
   * @return name of this logger instance
   */
  def getName() = base.getName()
  /**
   * Is the logger instance enabled for the TRACE level?
   *
   * @return True if this Logger is enabled for the TRACE level,
   *         false otherwise.
   * @since 1.4
   */
  def isTraceEnabled() = base.isTraceEnabled()
  /**
   * Log a message at the TRACE level.
   *
   * @param msg the message string to be logged
   * @since 1.4
   */
  def trace(msg: String) = if (isTraceEnabled) {
    base.trace(msg)
    XEvent.publish(XEvent.Incoming(this, XLevel.Trace, msg, None))
  }
  /**
   * Log a message at the TRACE level according to the specified format
   * and argument.
   * <p/>
   * <p>This form avoids superfluous object creation when the logger
   * is disabled for the TRACE level. </p>
   *
   * @param format the format string
   * @param arg    the argument
   * @since 1.4
   */
  def trace(format: String, arg: AnyRef) = if (isTraceEnabled) {
    val ft = MessageFormatter.format(format, arg)
    base.trace(ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Trace, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * Log a message at the TRACE level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous string concatenation when the logger
   * is disabled for the TRACE level. However, this variant incurs the hidden
   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for TRACE. The variants taking trace(String, Object) one and
   * trace(String, Object, Object) two arguments exist solely in order to avoid this hidden cost.</p>
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   * @since 1.4
   */
  def trace(format: String, arguments: AnyRef*): Unit = if (isTraceEnabled) {
    val ft = MessageFormatter.arrayFormat(format, arguments.toArray)
    base.trace(ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Trace, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * This method is similar to trace(String, Object, Object)
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def trace(format: String, arg1: AnyRef, arg2: AnyRef) =
    this.asInstanceOf[{ def trace(format: String, arguments: AnyRef*): Unit }].
      trace(format, arg1, arg2)
  /**
   * Log an exception (throwable) at the TRACE level with an
   * accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   * @since 1.4
   */
  def trace(msg: String, t: Throwable) = if (isTraceEnabled) {
    base.trace(msg, t)
    XEvent.publish(XEvent.Incoming(this, XLevel.Trace, msg, Option(t)))
  }
  /**
   * Similar to isTraceEnabled() method except that the
   * marker data is also taken into account.
   *
   * @param marker The marker data to take into consideration
   * @return True if this Logger is enabled for the TRACE level,
   *         false otherwise.
   *
   * @since 1.4
   */
  def isTraceEnabled(marker: Marker): Boolean = base.isTraceEnabled(marker)
  /**
   * Log a message with the specific Marker at the TRACE level.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message string to be logged
   * @since 1.4
   */
  def trace(marker: Marker, msg: String) = if (isTraceEnabled(marker)) {
    base.trace(marker, msg)
    XEvent.publish(XEvent.Incoming(this, XLevel.Trace, msg, None))
  }
  /**
   * This method is similar to trace(String, Object) method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg    the argument
   * @since 1.4
   */
  def trace(marker: Marker, format: String, arg: AnyRef) = if (isTraceEnabled(marker)) {
    val ft = MessageFormatter.format(format, arg)
    base.trace(marker, ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Trace, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * This method is similar to trace(String, Object...)
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param marker   the marker data specific to this log statement
   * @param format   the format string
   * @param argArray an array of arguments
   * @since 1.4
   */
  def trace(marker: Marker, format: String, arguments: AnyRef*) = if (isTraceEnabled(marker)) {
    val ft = MessageFormatter.arrayFormat(format, arguments.toArray)
    base.trace(marker, ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Trace, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * Log a message at the TRACE level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous object creation when the logger
   * is disabled for the TRACE level. </p>
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def trace(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef) =
    this.asInstanceOf[{ def trace(marker: Marker, format: String, arguments: AnyRef*): Unit }].
      trace(marker, format, arg1, arg2)
  /**
   * This method is similar to trace(String, Throwable) method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message accompanying the exception
   * @param t      the exception (throwable) to log
   * @since 1.4
   */
  def trace(marker: Marker, msg: String, t: Throwable) = if (isTraceEnabled(marker)) {
    base.trace(marker, msg, t)
    XEvent.publish(XEvent.Incoming(this, XLevel.Trace, msg, Option(t)))
  }
  /** Log a trace message with caller location */
  def traceWhere(msg: String): Unit = if (isTraceEnabled) {
    traceFuncS(msg)
  }
  /** Log a trace message with specific caller location */
  def traceWhere(msg: String, stackLine: Int): Unit = if (isTraceEnabled) {
    traceFuncSI(msg, stackLine)
  }

  /**
   * Is the logger instance enabled for the DEBUG level?
   *
   * @return True if this Logger is enabled for the DEBUG level,
   *         false otherwise.
   */
  def isDebugEnabled(): Boolean = base.isDebugEnabled()
  /**
   * Log a message at the DEBUG level.
   *
   * @param msg the message string to be logged
   */
  def debug(msg: String) = {
    base.debug(msg)
    XEvent.publish(XEvent.Incoming(this, XLevel.Debug, msg, None))
  }
  /**
   * Log a message at the DEBUG level according to the specified format
   * and argument.
   * <p/>
   * <p>This form avoids superfluous object creation when the logger
   * is disabled for the DEBUG level. </p>
   *
   * @param format the format string
   * @param arg    the argument
   */
  def debug(format: String, arg: AnyRef) = {
    val ft = MessageFormatter.format(format, arg)
    base.debug(ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Debug, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * Log a message at the DEBUG level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous string concatenation when the logger
   * is disabled for the DEBUG level. However, this variant incurs the hidden
   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for DEBUG. The variants taking
   * debug(String, Object) one and debug(String, Object, Object) two
   * arguments exist solely in order to avoid this hidden cost.</p>
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def debug(format: String, arguments: AnyRef*) = {
    val ft = MessageFormatter.arrayFormat(format, arguments.toArray)
    base.debug(ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Debug, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * This method is similar to debug(String, Object, Object)
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def debug(format: String, arg1: AnyRef, arg2: AnyRef) =
    this.asInstanceOf[{ def debug(format: String, arguments: AnyRef*): Unit }].
      debug(format, arg1, arg2)
  /**
   * Log an exception (throwable) at the DEBUG level with an
   * accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  def debug(msg: String, t: Throwable) = {
    base.debug(msg, t)
    XEvent.publish(XEvent.Incoming(this, XLevel.Debug, msg, Option(t)))
  }
  /**
   * Similar to isDebugEnabled() method except that the
   * marker data is also taken into account.
   *
   * @param marker The marker data to take into consideration
   * @return True if this Logger is enabled for the DEBUG level,
   *         false otherwise.
   */
  def isDebugEnabled(marker: Marker): Boolean = base.isDebugEnabled(marker)
  /**
   * Log a message with the specific Marker at the DEBUG level.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message string to be logged
   */
  def debug(marker: Marker, msg: String) = {
    base.debug(marker, msg)
    XEvent.publish(XEvent.Incoming(this, XLevel.Debug, msg, None))
  }
  /**
   * This method is similar to debug(String, Object) method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg    the argument
   */
  def debug(marker: Marker, format: String, arg: AnyRef) = {
    val ft = MessageFormatter.format(format, arg)
    base.debug(marker, ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Debug, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * This method is similar to debug(String, Object...)
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param marker    the marker data specific to this log statement
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def debug(marker: Marker, format: String, arguments: AnyRef*) = {
    val ft = MessageFormatter.arrayFormat(format, arguments.toArray)
    base.debug(marker, ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Debug, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * Log a message at the DEBUG level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous object creation when the logger
   * is disabled for the DEBUG level. </p>
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def debug(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef) =
    this.asInstanceOf[{ def debug(marker: Marker, format: String, arguments: AnyRef*): Unit }].
      debug(marker, format, arg1, arg2)
  /**
   * This method is similar to debug(String, Throwable) method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message accompanying the exception
   * @param t      the exception (throwable) to log
   */
  def debug(marker: Marker, msg: String, t: Throwable) = {
    base.debug(marker, msg, t)
    XEvent.publish(XEvent.Incoming(this, XLevel.Debug, msg, Option(t)))
  }
  /** Log a debug message with the caller location. */
  def debugWhere(msg: String) = debugFuncS(msg)
  /** Log a debug message with the specific caller location. */
  def debugWhere(msg: String, stackLine: Int) = debugFuncSI(msg, stackLine)

  /**
   * Is the logger instance enabled for the INFO level?
   *
   * @return True if this Logger is enabled for the INFO level,
   *         false otherwise.
   */
  def isInfoEnabled(): Boolean = base.isInfoEnabled()
  /**
   * Log a message at the INFO level.
   *
   * @param msg the message string to be logged
   */
  def info(msg: String) = {
    base.info(msg)
    XEvent.publish(XEvent.Incoming(this, XLevel.Info, msg, None))
  }
  /**
   * Log a message at the INFO level according to the specified format
   * and argument.
   * <p/>
   * <p>This form avoids superfluous object creation when the logger
   * is disabled for the INFO level. </p>
   *
   * @param format the format string
   * @param arg    the argument
   */
  def info(format: String, arg: AnyRef) = {
    val ft = MessageFormatter.format(format, arg)
    base.info(ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Info, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * Log a message at the INFO level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous string concatenation when the logger
   * is disabled for the INFO level. However, this variant incurs the hidden
   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for INFO. The variants taking
   * info(String, Object) one and info(String, Object, Object) two
   * arguments exist solely in order to avoid this hidden cost.</p>
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def info(format: String, arguments: AnyRef*) = {
    val ft = MessageFormatter.arrayFormat(format, arguments.toArray)
    base.info(ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Info, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * This method is similar to info(String, Object, Object)
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def info(format: String, arg1: AnyRef, arg2: AnyRef) =
    this.asInstanceOf[{ def info(format: String, arguments: AnyRef*): Unit }].
      info(format, arg1, arg2)
  /**
   * Log an exception (throwable) at the INFO level with an
   * accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  def info(msg: String, t: Throwable) = {
    base.info(msg, t)
    XEvent.publish(XEvent.Incoming(this, XLevel.Info, msg, Option(t)))
  }
  /**
   * Similar to isInfoEnabled() method except that the
   * marker data is also taken into account.
   *
   * @param marker The marker data to take into consideration
   * @return True if this Logger is enabled for the INFO level,
   *         false otherwise.
   */
  def isInfoEnabled(marker: Marker): Boolean = base.isInfoEnabled(marker)
  /**
   * Log a message with the specific Marker at the INFO level.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message string to be logged
   */
  def info(marker: Marker, msg: String) = {
    base.info(marker, msg)
    XEvent.publish(XEvent.Incoming(this, XLevel.Info, msg, None))
  }
  /**
   * This method is similar to info(String, Object) method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg    the argument
   */
  def info(marker: Marker, format: String, arg: AnyRef) = {
    val ft = MessageFormatter.format(format, arg)
    base.info(marker, ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Info, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * This method is similar to info(String, Object...)
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param marker    the marker data specific to this log statement
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def info(marker: Marker, format: String, arguments: AnyRef*) = {
    val ft = MessageFormatter.arrayFormat(format, arguments.toArray)
    base.info(marker, ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Info, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * Log a message at the INFO level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous object creation when the logger
   * is disabled for the INFO level. </p>
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def info(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef) =
    this.asInstanceOf[{ def info(marker: Marker, format: String, arguments: AnyRef*): Unit }].
      info(marker, format, arg1, arg2)
  /**
   * This method is similar to info(String, Throwable) method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message accompanying the exception
   * @param t      the exception (throwable) to log
   */
  def info(marker: Marker, msg: String, t: Throwable) = {
    base.info(marker, msg, t)
    XEvent.publish(XEvent.Incoming(this, XLevel.Info, msg, Option(t)))
  }
  /** Log a info message with the caller location. */
  def infoWhere(msg: String) = infoFuncS(msg)
  /** Log a info message with the specific caller location. */
  def infoWhere(msg: String, stackLine: Int) = infoFuncSI(msg, stackLine)

  /**
   * Is the logger instance enabled for the WARN level?
   *
   * @return True if this Logger is enabled for the WARN level,
   *         false otherwise.
   */
  def isWarnEnabled(): Boolean = base.isWarnEnabled()
  /**
   * Log a message at the WARN level.
   *
   * @param msg the message string to be logged
   */
  def warn(msg: String) = {
    base.warn(msg)
    XEvent.publish(XEvent.Incoming(this, XLevel.Warn, msg, None))
  }
  /**
   * Log a message at the WARN level according to the specified format
   * and argument.
   * <p/>
   * <p>This form avoids superfluous object creation when the logger
   * is disabled for the WARN level. </p>
   *
   * @param format the format string
   * @param arg    the argument
   */
  def warn(format: String, arg: AnyRef) = {
    val ft = MessageFormatter.format(format, arg)
    base.warn(ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Warn, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * Log a message at the WARN level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous string concatenation when the logger
   * is disabled for the WARN level. However, this variant incurs the hidden
   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for WARN. The variants taking
   * warn(String, Object) one and warn(String, Object, Object) two
   * arguments exist solely in order to avoid this hidden cost.</p>
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def warn(format: String, arguments: AnyRef*) = {
    val ft = MessageFormatter.arrayFormat(format, arguments.toArray)
    base.warn(ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Warn, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * This method is similar to warn(String, Object, Object)
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def warn(format: String, arg1: AnyRef, arg2: AnyRef) =
    this.asInstanceOf[{ def warn(format: String, arguments: AnyRef*): Unit }].
      warn(format, arg1, arg2)
  /**
   * Log an exception (throwable) at the WARN level with an
   * accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  def warn(msg: String, t: Throwable) = {
    base.warn(msg, t)
    XEvent.publish(XEvent.Incoming(this, XLevel.Warn, msg, Option(t)))
  }
  /**
   * Similar to isWarnEnabled() method except that the
   * marker data is also taken into account.
   *
   * @param marker The marker data to take into consideration
   * @return True if this Logger is enabled for the WARN level,
   *         false otherwise.
   */
  def isWarnEnabled(marker: Marker): Boolean = base.isWarnEnabled(marker)
  /**
   * Log a message with the specific Marker at the WARN level.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message string to be logged
   */
  def warn(marker: Marker, msg: String) = {
    base.warn(marker, msg)
    XEvent.publish(XEvent.Incoming(this, XLevel.Warn, msg, None))
  }
  /**
   * This method is similar to warn(String, Object) method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg    the argument
   */
  def warn(marker: Marker, format: String, arg: AnyRef) = {
    val ft = MessageFormatter.format(format, arg)
    base.warn(marker, ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Warn, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * This method is similar to warn(String, Object...)
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param marker    the marker data specific to this log statement
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def warn(marker: Marker, format: String, arguments: AnyRef*) = {
    val ft = MessageFormatter.arrayFormat(format, arguments.toArray)
    base.warn(marker, ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Warn, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * Log a message at the WARN level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous object creation when the logger
   * is disabled for the WARN level. </p>
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def warn(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef) =
    this.asInstanceOf[{ def warn(marker: Marker, format: String, arguments: AnyRef*): Unit }].
      warn(marker, format, arg1, arg2)
  /**
   * This method is similar to warn(String, Throwable) method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message accompanying the exception
   * @param t      the exception (throwable) to log
   */
  def warn(marker: Marker, msg: String, t: Throwable) = {
    base.warn(marker, msg, t)
    XEvent.publish(XEvent.Incoming(this, XLevel.Warn, msg, Option(t)))
  }
  /** Log a warn message with the caller location. */
  def warnWhere(msg: String) = warnFuncS(msg)
  /** Log a warn message with the specific caller location. */
  def warnWhere(msg: String, stackLine: Int) = warnFuncSI(msg, stackLine)

  /**
   * Is the logger instance enabled for the ERROR level?
   *
   * @return True if this Logger is enabled for the ERROR level,
   *         false otherwise.
   */
  def isErrorEnabled(): Boolean = base.isErrorEnabled()
  /**
   * Log a message at the ERROR level.
   *
   * @param msg the message string to be logged
   */
  def error(msg: String) = {
    base.error(msg)
    XEvent.publish(XEvent.Incoming(this, XLevel.Error, msg, None))
  }
  /**
   * Log a message at the ERROR level according to the specified format
   * and argument.
   * <p/>
   * <p>This form avoids superfluous object creation when the logger
   * is disabled for the ERROR level. </p>
   *
   * @param format the format string
   * @param arg    the argument
   */
  def error(format: String, arg: AnyRef) = {
    val ft = MessageFormatter.format(format, arg)
    base.error(ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Error, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * Log a message at the ERROR level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous string concatenation when the logger
   * is disabled for the ERROR level. However, this variant incurs the hidden
   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for ERROR. The variants taking
   * error(String, Object) one and error(String, Object, Object) two
   * arguments exist solely in order to avoid this hidden cost.</p>
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def error(format: String, arguments: AnyRef*) = {
    val ft = MessageFormatter.arrayFormat(format, arguments.toArray)
    base.error(ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Error, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * This method is similar to error(String, Object, Object)
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def error(format: String, arg1: AnyRef, arg2: AnyRef) =
    this.asInstanceOf[{ def error(format: String, arguments: AnyRef*): Unit }].
      error(format, arg1, arg2)
  /**
   * Log an exception (throwable) at the ERROR level with an
   * accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  def error(msg: String, t: Throwable) = {
    base.error(msg, t)
    XEvent.publish(XEvent.Incoming(this, XLevel.Error, msg, Option(t)))
  }
  /**
   * Similar to isErrorEnabled() method except that the
   * marker data is also taken into account.
   *
   * @param marker The marker data to take into consideration
   * @return True if this Logger is enabled for the ERROR level,
   *         false otherwise.
   */
  def isErrorEnabled(marker: Marker): Boolean = base.isErrorEnabled(marker)
  /**
   * Log a message with the specific Marker at the ERROR level.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message string to be logged
   */
  def error(marker: Marker, msg: String) = {
    base.error(marker, msg)
    XEvent.publish(XEvent.Incoming(this, XLevel.Error, msg, None))
  }
  /**
   * This method is similar to error(String, Object) method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg    the argument
   */
  def error(marker: Marker, format: String, arg: AnyRef) = {
    val ft = MessageFormatter.format(format, arg)
    base.error(marker, ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Error, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * This method is similar to error(String, Object...)
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param marker    the marker data specific to this log statement
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def error(marker: Marker, format: String, arguments: AnyRef*) = {
    val ft = MessageFormatter.arrayFormat(format, arguments.toArray)
    base.error(marker, ft.getMessage(), ft.getThrowable())
    XEvent.publish(XEvent.Incoming(this, XLevel.Error, ft.getMessage(), Option(ft.getThrowable())))
  }
  /**
   * Log a message at the ERROR level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous object creation when the logger
   * is disabled for the ERROR level. </p>
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def error(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef) =
    this.asInstanceOf[{ def error(marker: Marker, format: String, arguments: AnyRef*): Unit }].
      error(marker, format, arg1, arg2)
  /**
   * This method is similar to error(String, Throwable) method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message accompanying the exception
   * @param t      the exception (throwable) to log
   */
  def error(marker: Marker, msg: String, t: Throwable) = {
    base.error(marker, msg, t)
    XEvent.publish(XEvent.Incoming(this, XLevel.Error, msg, Option(t)))
  }
  /** Log a error message with the caller location. */
  def errorWhere(msg: String) = errorFuncS(msg)
  /** Log a error message with the specific caller location. */
  def errorWhere(msg: String, stackLine: Int) = errorFuncSI(msg, stackLine)

  protected def logWhere(msg: String, f1: (String, Throwable) ⇒ Unit, f2: (String ⇒ Unit))(stackLine: Int) {
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
