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

package org.digimead.digi.lib.log.api

import org.slf4j.Marker

/*
 * The reasons of so straight and clear architecture are
 * first:
 *   public void trace(String format, Object arg1, Object arg2)
 *   public void trace(String format, Object... arguments)
 *   is the same for Scala compiler :-/
 * second:
 *   Simple implicit redirection to 'base: org.slf4j.Logger' make mock factories are useless,
 *   so testing with logging subsystem gets tricky (welcome reflection, substitution with classloaders and so on).
 */
/**
 * Digi logger API based on SLF4J
 */
trait RichLogger extends org.slf4j.Logger {
  val base: org.slf4j.Logger
  val isWhereEnabled: Boolean

  /** fast look while development, highlight it in your IDE */
  def ___gaze(msg: String)
  /** fast look while development, highlight it in your IDE */
  def ___glance(msg: String)
  /** error with stack trace */
  def fatal(msg: String)
  /** error with stack trace and external exception */
  def fatal(msg: String, t: Throwable)

  /**
   * Return the name of this <code>Logger</code> instance.
   * @return name of this logger instance
   */
  def getName(): String
  /**
   * Is the logger instance enabled for the TRACE level?
   *
   * @return True if this Logger is enabled for the TRACE level,
   *         false otherwise.
   * @since 1.4
   */
  def isTraceEnabled(): Boolean
  /**
   * Log a message at the TRACE level.
   *
   * @param msg the message string to be logged
   * @since 1.4
   */
  def trace(msg: String)
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
  def trace(format: String, arg: AnyRef)
  /**
   * Log a message at the TRACE level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous string concatenation when the logger
   * is disabled for the TRACE level. However, this variant incurs the hidden
   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for TRACE. The variants taking {@link #trace(String, Object) one} and
   * {@link #trace(String, Object, Object) two} arguments exist solely in order to avoid this hidden cost.</p>
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   * @since 1.4
   */
  def trace(format: String, arguments: AnyRef*)
  /**
   * This method is similar to {@link #trace(String, Object, Object)}
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def trace(format: String, arg1: AnyRef, arg2: AnyRef)
  /**
   * Log an exception (throwable) at the TRACE level with an
   * accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   * @since 1.4
   */
  def trace(msg: String, t: Throwable)
  /**
   * Similar to {@link #isTraceEnabled()} method except that the
   * marker data is also taken into account.
   *
   * @param marker The marker data to take into consideration
   * @return True if this Logger is enabled for the TRACE level,
   *         false otherwise.
   *
   * @since 1.4
   */
  def isTraceEnabled(marker: Marker): Boolean
  /**
   * Log a message with the specific Marker at the TRACE level.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message string to be logged
   * @since 1.4
   */
  def trace(marker: Marker, msg: String)
  /**
   * This method is similar to {@link #trace(String, Object)} method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg    the argument
   * @since 1.4
   */
  def trace(marker: Marker, format: String, arg: AnyRef)
  /**
   * This method is similar to {@link #trace(String, Object...)}
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param marker   the marker data specific to this log statement
   * @param format   the format string
   * @param argArray an array of arguments
   * @since 1.4
   */
  def trace(marker: Marker, format: String, arg: AnyRef*)
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
  def trace(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef)
  /**
   * This method is similar to {@link #trace(String, Throwable)} method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message accompanying the exception
   * @param t      the exception (throwable) to log
   * @since 1.4
   */
  def trace(marker: Marker, msg: String, t: Throwable)
  /** Log a trace message with the caller location. */
  def traceWhere(msg: String)
  /** Log a trace message with the specific caller location. */
  def traceWhere(msg: String, stackLine: Int)

  /**
   * Is the logger instance enabled for the DEBUG level?
   *
   * @return True if this Logger is enabled for the DEBUG level,
   *         false otherwise.
   */
  def isDebugEnabled(): Boolean
  /**
   * Log a message at the DEBUG level.
   *
   * @param msg the message string to be logged
   */
  def debug(msg: String)
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
  def debug(format: String, arg: AnyRef)
  /**
   * Log a message at the DEBUG level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous string concatenation when the logger
   * is disabled for the DEBUG level. However, this variant incurs the hidden
   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for DEBUG. The variants taking
   * {@link #debug(String, Object) one} and {@link #debug(String, Object, Object) two}
   * arguments exist solely in order to avoid this hidden cost.</p>
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def debug(format: String, arguments: AnyRef*)
  /**
   * This method is similar to {@link #debug(String, Object, Object)}
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def debug(format: String, arg1: AnyRef, arg2: AnyRef)
  /**
   * Log an exception (throwable) at the DEBUG level with an
   * accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  def debug(msg: String, t: Throwable)
  /**
   * Similar to {@link #isDebugEnabled()} method except that the
   * marker data is also taken into account.
   *
   * @param marker The marker data to take into consideration
   * @return True if this Logger is enabled for the DEBUG level,
   *         false otherwise.
   */
  def isDebugEnabled(marker: Marker): Boolean
  /**
   * Log a message with the specific Marker at the DEBUG level.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message string to be logged
   */
  def debug(marker: Marker, msg: String)
  /**
   * This method is similar to {@link #debug(String, Object)} method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg    the argument
   */
  def debug(marker: Marker, format: String, arg: AnyRef)
  /**
   * This method is similar to {@link #debug(String, Object...)}
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param marker    the marker data specific to this log statement
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def debug(marker: Marker, format: String, arguments: AnyRef*)
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
  def debug(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef)
  /**
   * This method is similar to {@link #debug(String, Throwable)} method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message accompanying the exception
   * @param t      the exception (throwable) to log
   */
  def debug(marker: Marker, msg: String, t: Throwable)
  /** Log a debug message with the caller location. */
  def debugWhere(msg: String)
  /** Log a debug message with the specific caller location. */
  def debugWhere(msg: String, stackLine: Int)

  /**
   * Is the logger instance enabled for the INFO level?
   *
   * @return True if this Logger is enabled for the INFO level,
   *         false otherwise.
   */
  def isInfoEnabled(): Boolean
  /**
   * Log a message at the INFO level.
   *
   * @param msg the message string to be logged
   */
  def info(msg: String)
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
  def info(format: String, arg: AnyRef)
  /**
   * Log a message at the INFO level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous string concatenation when the logger
   * is disabled for the INFO level. However, this variant incurs the hidden
   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for INFO. The variants taking
   * {@link #info(String, Object) one} and {@link #info(String, Object, Object) two}
   * arguments exist solely in order to avoid this hidden cost.</p>
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def info(format: String, arguments: AnyRef*)
  /**
   * This method is similar to {@link #info(String, Object, Object)}
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def info(format: String, arg1: AnyRef, arg2: AnyRef)
  /**
   * Log an exception (throwable) at the INFO level with an
   * accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  def info(msg: String, t: Throwable)
  /**
   * Similar to {@link #isInfoEnabled()} method except that the
   * marker data is also taken into account.
   *
   * @param marker The marker data to take into consideration
   * @return True if this Logger is enabled for the INFO level,
   *         false otherwise.
   */
  def isInfoEnabled(marker: Marker): Boolean
  /**
   * Log a message with the specific Marker at the INFO level.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message string to be logged
   */
  def info(marker: Marker, msg: String)
  /**
   * This method is similar to {@link #info(String, Object)} method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg    the argument
   */
  def info(marker: Marker, format: String, arg: AnyRef)
  /**
   * This method is similar to {@link #info(String, Object...)}
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param marker    the marker data specific to this log statement
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def info(marker: Marker, format: String, arguments: AnyRef*)
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
  def info(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef)
  /**
   * This method is similar to {@link #info(String, Throwable)} method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message accompanying the exception
   * @param t      the exception (throwable) to log
   */
  def info(marker: Marker, msg: String, t: Throwable)
  /** Log a info message with the caller location. */
  def infoWhere(msg: String)
  /** Log a info message with the specific caller location. */
  def infoWhere(msg: String, stackLine: Int)

  /**
   * Is the logger instance enabled for the WARN level?
   *
   * @return True if this Logger is enabled for the WARN level,
   *         false otherwise.
   */
  def isWarnEnabled(): Boolean
  /**
   * Log a message at the WARN level.
   *
   * @param msg the message string to be logged
   */
  def warn(msg: String)
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
  def warn(format: String, arg: AnyRef)
  /**
   * Log a message at the WARN level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous string concatenation when the logger
   * is disabled for the WARN level. However, this variant incurs the hidden
   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for WARN. The variants taking
   * {@link #warn(String, Object) one} and {@link #warn(String, Object, Object) two}
   * arguments exist solely in order to avoid this hidden cost.</p>
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def warn(format: String, arguments: AnyRef*)
  /**
   * This method is similar to {@link #warn(String, Object, Object)}
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def warn(format: String, arg1: AnyRef, arg2: AnyRef)
  /**
   * Log an exception (throwable) at the WARN level with an
   * accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  def warn(msg: String, t: Throwable)
  /**
   * Similar to {@link #isWarnEnabled()} method except that the
   * marker data is also taken into account.
   *
   * @param marker The marker data to take into consideration
   * @return True if this Logger is enabled for the WARN level,
   *         false otherwise.
   */
  def isWarnEnabled(marker: Marker): Boolean
  /**
   * Log a message with the specific Marker at the WARN level.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message string to be logged
   */
  def warn(marker: Marker, msg: String)
  /**
   * This method is similar to {@link #warn(String, Object)} method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg    the argument
   */
  def warn(marker: Marker, format: String, arg: AnyRef)
  /**
   * This method is similar to {@link #warn(String, Object...)}
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param marker    the marker data specific to this log statement
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def warn(marker: Marker, format: String, arguments: AnyRef*)
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
  def warn(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef)
  /**
   * This method is similar to {@link #warn(String, Throwable)} method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message accompanying the exception
   * @param t      the exception (throwable) to log
   */
  def warn(marker: Marker, msg: String, t: Throwable)
  /** Log a warn message with the caller location. */
  def warnWhere(msg: String)
  /** Log a warn message with the specific caller location. */
  def warnWhere(msg: String, stackLine: Int)

  /**
   * Is the logger instance enabled for the ERROR level?
   *
   * @return True if this Logger is enabled for the ERROR level,
   *         false otherwise.
   */
  def isErrorEnabled(): Boolean
  /**
   * Log a message at the ERROR level.
   *
   * @param msg the message string to be logged
   */
  def error(msg: String)
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
  def error(format: String, arg: AnyRef)
  /**
   * Log a message at the ERROR level according to the specified format
   * and arguments.
   * <p/>
   * <p>This form avoids superfluous string concatenation when the logger
   * is disabled for the ERROR level. However, this variant incurs the hidden
   * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for ERROR. The variants taking
   * {@link #error(String, Object) one} and {@link #error(String, Object, Object) two}
   * arguments exist solely in order to avoid this hidden cost.</p>
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def error(format: String, arguments: AnyRef*)
  /**
   * This method is similar to {@link #error(String, Object, Object)}
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  def error(format: String, arg1: AnyRef, arg2: AnyRef)
  /**
   * Log an exception (throwable) at the ERROR level with an
   * accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  def error(msg: String, t: Throwable)
  /**
   * Similar to {@link #isErrorEnabled()} method except that the
   * marker data is also taken into account.
   *
   * @param marker The marker data to take into consideration
   * @return True if this Logger is enabled for the ERROR level,
   *         false otherwise.
   */
  def isErrorEnabled(marker: Marker): Boolean
  /**
   * Log a message with the specific Marker at the ERROR level.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message string to be logged
   */
  def error(marker: Marker, msg: String)
  /**
   * This method is similar to {@link #error(String, Object)} method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param format the format string
   * @param arg    the argument
   */
  def error(marker: Marker, format: String, arg: AnyRef)
  /**
   * This method is similar to {@link #error(String, Object...)}
   * method except that the marker data is also taken into
   * consideration.
   *
   * @param marker    the marker data specific to this log statement
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  def error(marker: Marker, format: String, arguments: AnyRef*)
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
  def error(marker: Marker, format: String, arg1: AnyRef, arg2: AnyRef)
  /**
   * This method is similar to {@link #error(String, Throwable)} method except that the
   * marker data is also taken into consideration.
   *
   * @param marker the marker data specific to this log statement
   * @param msg    the message accompanying the exception
   * @param t      the exception (throwable) to log
   */
  def error(marker: Marker, msg: String, t: Throwable)
  /** Log a error message with the caller location. */
  def errorWhere(msg: String)
  /** Log a error message with the specific caller location. */
  def errorWhere(msg: String, stackLine: Int)
}
