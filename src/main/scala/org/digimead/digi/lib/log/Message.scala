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

package org.digimead.digi.lib.log

import java.util.Date

class Message(val date: Date,
  val tid: Long,
  val level: Record.Level,
  val tag: String,
  val message: String,
  val throwable: Option[Throwable],
  val pid: Int) extends Record.Message {
  override def toString = "%s P%05d T%05d %s %-24s %s".format(dateFormat.format(date),
    pid, tid, level.toString.charAt(0), tag + ":", message)
}
