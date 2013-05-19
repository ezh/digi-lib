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

package org.digimead.digi.lib

import java.text.SimpleDateFormat
import java.util.Date

import scala.annotation.implicitNotFound

import org.digimead.digi.lib.log.Logging
import org.digimead.digi.lib.log.MDC
import org.digimead.digi.lib.log.NDC
import org.digimead.digi.lib.log.Record
import org.digimead.digi.lib.log.logger.RichLogger
import org.slf4j.LoggerFactory

import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.NewBindingModule

package object log {
  private[log] lazy val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")
  lazy val default = new NewBindingModule(module => {
    module.bind[Option[Logging.BufferedLogThread]] identifiedBy "Log.BufferedThread" toSingle { None }
    module.bind[SimpleDateFormat] identifiedBy "Log.Record.DateFormat" toSingle { dateFormat }
    module.bind[Int] identifiedBy "Log.Record.PID" toSingle { -1 }
    module.bind[Record.MessageBuilder] identifiedBy "Log.Record.Builder" toSingle { (date: Date, tid: Long,
      level: Record.Level, tag: String, message: String, throwable: Option[Throwable], pid: Int) =>
      new Message(date, tid, level, tag, message, throwable, pid)
    }
    module.bind[Record] toModuleSingle { implicit module => new Record }
    module.bind[(String) => RichLogger] identifiedBy "Log.Builder" toProvider ((module: BindingModule) => {
      def isTraceWhereEnabled = module.injectOptional[Boolean](Some("Log.TraceWhereEnabled")) getOrElse false
      (name: String) => new RichLogger(LoggerFactory.getLogger(name), isTraceWhereEnabled)
    })
    module.bind[Logging] toModuleSingle { implicit module => new Logging }
  })
  lazy val defaultWithDC = new NewBindingModule(module => {
    module.bind[Record.MessageBuilder] identifiedBy "Log.Record.Builder" toSingle { (date: Date, tid: Long,
      level: Record.Level, tag: String, message: String, throwable: Option[Throwable], pid: Int) =>
      new Message(date, tid, level, tag, message + " " + getMDC + getNDC, throwable, pid)
    }

    def getMDC() = {
      val mdc = MDC.getSeq.map(t => t._1 + "=" + t._2).mkString(", ")
      if (mdc.isEmpty()) mdc else "{" + mdc + "}"
    }
    def getNDC() = {
      val ndc = NDC.getSeq.mkString(", ")
      if (ndc.isEmpty()) ndc else "{" + ndc + "}"
    }
  }) ~ default
  DependencyInjection.setPersistentInjectable("org.digimead.digi.lib.log.Logging$DI$")
}

package log {
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
}
