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
import java.text.SimpleDateFormat

class Record(
  val date: Date,
  val tid: Long,
  val level: Record.Level,
  val tag: String,
  val message: String,
  val throwable: Option[Throwable],
  val pid: Int) {
  override def toString = "%s P%05d T%05d %s %-24s %s".format(Record.dateFormat.format(date), pid, tid, level.toString.charAt(0), tag + ":", message)
}

object Record {
  private var recordBuilder: (Date, Long, Level, String, String, Option[Throwable], Int) => Record = null
  private var pid = -1
  private var dateFormat: SimpleDateFormat = null
  def apply(date: Date, tid: Long, level: Level, tag: String, message: String) =
    recordBuilder(date, tid, level, tag, message, None, pid)
  def apply(date: Date, tid: Long, level: Level, tag: String, message: String, throwable: Option[Throwable]) =
    recordBuilder(date, tid, level, tag, message, throwable, pid)
  def apply(date: Date, tid: Long, level: Level, tag: String, message: String, throwable: Option[Throwable], pid: Int) =
    recordBuilder(date, tid, level, tag, message, throwable, pid)
  def init(arg: Init) = synchronized {
    recordBuilder = arg.recordBuilder
    pid = arg.pid
    dateFormat = arg.dateFormat
  }
  sealed trait Level
  object Level {
    case object Trace extends Level
    case object Debug extends Level
    case object Info extends Level
    case object Warn extends Level
    case object Error extends Level
  }
  trait Init {
    val recordBuilder: (Date, Long, Level, String, String, Option[Throwable], Int) => Record
    val pid: Int
    val dateFormat: SimpleDateFormat
  }
  class DefaultInit extends Init {
    val recordBuilder = (date: Date, tid: Long, level: Level, tag: String, message: String, throwable: Option[Throwable], pid: Int) =>
      new Record(date, tid, level, tag, message, throwable, pid)
    val pid = -1
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")
  }
}
