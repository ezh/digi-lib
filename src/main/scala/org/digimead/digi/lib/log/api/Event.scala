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

import scala.collection.mutable.Publisher

sealed trait Event

object Event extends Publisher[Event] {
  override protected[log] def publish(event: Event) = try {
    super.publish(event)
  } catch {
    // This catches all Throwables because we want to record exception to log file
    case e: Throwable =>
      org.digimead.digi.lib.log.Logging.inner.commonLogger.error(e.getMessage(), e)
      e.printStackTrace() // maybe end user could copy'n'paste it for us
  }
  case class Incoming(val logger: RichLogger, val level: Level, val message: String, throwable: Option[Throwable]) extends Event
  case class Outgoing(val record: Message) extends Event
  case class RegisterLogger(val logger: RichLogger) extends Event
}
