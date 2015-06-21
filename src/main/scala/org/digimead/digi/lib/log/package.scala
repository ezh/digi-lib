/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012-2014 Alexey Aksenov ezh@ezh.msk.ru
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

import com.escalatesoft.subcut.inject.{ BindingModule, NewBindingModule }
import java.text.SimpleDateFormat
import java.util.Date
import org.digimead.digi.lib.log.{ Logging, MDC, NDC }
import org.digimead.digi.lib.log.api.{ XLevel, XMessage, XRichLogger }
import org.digimead.digi.lib.log.logger.BaseLogger
import org.slf4j.LoggerFactory
import scala.annotation.implicitNotFound

package object log {
  private[log] lazy val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")
  lazy val default = new NewBindingModule(module ⇒ {
    module.bind[Option[Logging.BufferedLogThread]] identifiedBy "Log.BufferedThread" toSingle { None }
    module.bind[SimpleDateFormat] identifiedBy "Log.Record.DateFormat" toSingle { dateFormat }
    module.bind[Int] identifiedBy "Log.Record.PID" toSingle { -1 }
    module.bind[XMessage.MessageBuilder] identifiedBy "Log.Record.Builder" toSingle { (date: Date, tid: Long,
      level: XLevel, tag: String, tagClass: Class[_], message: String, throwable: Option[Throwable], pid: Int) ⇒
      new Message(date, tid, level, tag, tagClass, message, throwable, pid)
    }
    module.bind[Record] toModuleSingle { implicit module ⇒ new Record }
    module.bind[(String, Class[_]) ⇒ XRichLogger] identifiedBy "Log.Builder" toProvider ((module: BindingModule) ⇒ {
      def isWhereEnabled = module.injectOptional[Boolean](Some("Log.TraceWhereEnabled")) getOrElse false
      (name: String, classTag: Class[_]) ⇒
        val richLogger = new org.digimead.digi.lib.log.logger.RichLogger(LoggerFactory.getLogger(name), isWhereEnabled)
        // add class tag if possible
        richLogger.base match {
          case base: BaseLogger ⇒ base.loggerClass = classTag
          case log4j if log4j.getClass.getName == "org.slf4j.impl.Log4jLoggerAdapter" ⇒ // skip Log4jLoggerAdapter logger - API is very poor
          case other: org.slf4j.Logger ⇒ // skip other unknown logger
        }
        richLogger
    })
    module.bind[Logging] toModuleSingle { implicit module ⇒ new Logging }
  })
  lazy val defaultWithDC = new NewBindingModule(module ⇒ {
    module.bind[XMessage.MessageBuilder] identifiedBy "Log.Record.Builder" toSingle { (date: Date, tid: Long,
      level: XLevel, tag: String, tagClass: Class[_], message: String, throwable: Option[Throwable], pid: Int) ⇒
      new Message(date, tid, level, tag, tagClass, message + " " + getMDC + getNDC, throwable, pid)
    }

    def getMDC() = {
      val mdc = MDC.getSeq.map(t ⇒ t._1 + "=" + t._2).mkString(", ")
      if (mdc.isEmpty()) mdc else "{" + mdc + "}"
    }
    def getNDC() = {
      val ndc = NDC.getSeq.mkString(", ")
      if (ndc.isEmpty()) ndc else "{" + ndc + "}"
    }
  }) ~ default
  DependencyInjection.setPersistentInjectable("org.digimead.digi.lib.log.Logging$DI$")
}

