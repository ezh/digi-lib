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

import java.util.Date

import org.digimead.digi.lib.log.Logging
import org.digimead.digi.lib.log.Record

/** Base class that absolutely compatible with org.slf4j.Logger */
class BaseLogger(val loggerName: java.lang.String,
  val isTraceEnabled: Boolean,
  val isDebugEnabled: Boolean,
  val isInfoEnabled: Boolean,
  val isWarnEnabled: Boolean,
  val isErrorEnabled: Boolean) extends AbstractBaseLogger(loggerName) {
  /*
   * What is this public volatile field doing here?
   * - from the one side we must keep BaseLogger as close as possible to original org.slf4j.Logger,
   *   so we able to instantiate it only with loggerName. loggerName is verbose, end user representation
   *   like "MyLogger" instead of "clazz@anon$$1$x.fn2" based on class name.
   * - from the other side we want to pass an original class to the end point because of user
   *   that may want to filter everything that begins with "a.b.c."
   */
  /** Internal class tag for end user processing */
  @volatile var loggerClass: Class[_] = classOf[AnyRef]
  def trace(msg: String): Unit = if (isTraceEnabled)
    Logging.inner.offer(Logging.inner.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName,
      loggerClass, msg, None, Logging.inner.record.pid))
  def trace(msg: String, t: Throwable): Unit = if (isTraceEnabled)
    Logging.inner.offer(Logging.inner.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName,
      loggerClass, msg, Some(t), Logging.inner.record.pid))
  def debug(msg: String): Unit = if (isDebugEnabled)
    Logging.inner.offer(Logging.inner.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName,
      loggerClass, msg, None, Logging.inner.record.pid))
  def debug(msg: String, t: Throwable): Unit = if (isDebugEnabled)
    Logging.inner.offer(Logging.inner.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName,
      loggerClass, msg, Some(t), Logging.inner.record.pid))
  def info(msg: String): Unit = if (isInfoEnabled)
    Logging.inner.offer(Logging.inner.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName,
      loggerClass, msg, None, Logging.inner.record.pid))
  def info(msg: String, t: Throwable): Unit = if (isInfoEnabled)
    Logging.inner.offer(Logging.inner.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName,
      loggerClass, msg, Some(t), Logging.inner.record.pid))
  def warn(msg: String): Unit = if (isWarnEnabled)
    Logging.inner.offer(Logging.inner.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName,
      loggerClass, msg, None, Logging.inner.record.pid))
  def warn(msg: String, t: Throwable): Unit = if (isWarnEnabled)
    Logging.inner.offer(Logging.inner.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName,
      loggerClass, msg, Some(t), Logging.inner.record.pid))
  def error(msg: String): Unit = if (isErrorEnabled)
    Logging.inner.offer(Logging.inner.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName,
      loggerClass, msg, None, Logging.inner.record.pid))
  // always enabled
  def error(msg: String, t: Throwable): Unit =
    Logging.inner.offer(Logging.inner.record.builder(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName,
      loggerClass, msg, Some(t), Logging.inner.record.pid))
}
