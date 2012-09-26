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

import java.util.Date

import org.digimead.digi.lib.log.Logging
import org.digimead.digi.lib.log.Record

class BaseLogger(val loggerName: java.lang.String) extends AbstractBaseLogger(loggerName) {
  def isTraceEnabled(): Boolean = Logging.isTraceEnabled
  def trace(msg: String): Unit = if (isTraceEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName, msg))
  def trace(msg: String, t: Throwable): Unit = if (isTraceEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName, msg, Some(t)))
  def isDebugEnabled(): Boolean = Logging.isDebugEnabled
  def debug(msg: String): Unit = if (isDebugEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName, msg))
  def debug(msg: String, t: Throwable): Unit = if (isDebugEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName, msg, Some(t)))
  def isInfoEnabled: Boolean = Logging.isInfoEnabled
  def info(msg: String): Unit = if (isInfoEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName, msg))
  def info(msg: String, t: Throwable): Unit = if (isInfoEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName, msg, Some(t)))
  def isWarnEnabled: Boolean = Logging.isWarnEnabled
  def warn(msg: String): Unit = if (isWarnEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName, msg))
  def warn(msg: String, t: Throwable): Unit = if (isWarnEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName, msg, Some(t)))
  def isErrorEnabled: Boolean = Logging.isErrorEnabled
  def error(msg: String): Unit = if (isErrorEnabled)
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName, msg))
  // always enabled
  def error(msg: String, t: Throwable): Unit =
    Logging.offer(Record(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName, msg, Some(t)))
}
