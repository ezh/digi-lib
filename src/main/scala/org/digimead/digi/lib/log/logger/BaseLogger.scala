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

import org.digimead.digi.lib.log.Record

class BaseLogger(val loggerName: java.lang.String,
  val isTraceWhereEnabled: Boolean,
  val isTraceEnabled: Boolean,
  val isDebugEnabled: Boolean,
  val isInfoEnabled: Boolean,
  val isWarnEnabled: Boolean,
  val isErrorEnabled: Boolean,
  val pid: Int,
  val builder: Record.MessageBuilder,
  val offer: (Record.Message) => Unit) extends AbstractBaseLogger(loggerName) {
  def trace(msg: String): Unit = if (isTraceEnabled)
    offer(builder(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName, msg, None, pid))
  def trace(msg: String, t: Throwable): Unit = if (isTraceEnabled)
    offer(builder(new Date(), Thread.currentThread.getId, Record.Level.Trace, loggerName, msg, Some(t), pid))
  def debug(msg: String): Unit = if (isDebugEnabled)
    offer(builder(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName, msg, None, pid))
  def debug(msg: String, t: Throwable): Unit = if (isDebugEnabled)
    offer(builder(new Date(), Thread.currentThread.getId, Record.Level.Debug, loggerName, msg, Some(t), pid))
  def info(msg: String): Unit = if (isInfoEnabled)
    offer(builder(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName, msg, None, pid))
  def info(msg: String, t: Throwable): Unit = if (isInfoEnabled)
    offer(builder(new Date(), Thread.currentThread.getId, Record.Level.Info, loggerName, msg, Some(t), pid))
  def warn(msg: String): Unit = if (isWarnEnabled)
    offer(builder(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName, msg, None, pid))
  def warn(msg: String, t: Throwable): Unit = if (isWarnEnabled)
    offer(builder(new Date(), Thread.currentThread.getId, Record.Level.Warn, loggerName, msg, Some(t), pid))
  def error(msg: String): Unit = if (isErrorEnabled)
    offer(builder(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName, msg, None, pid))
  // always enabled
  def error(msg: String, t: Throwable): Unit =
    offer(builder(new Date(), Thread.currentThread.getId, Record.Level.Error, loggerName, msg, Some(t), pid))
}
