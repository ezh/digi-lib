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

import scala.annotation.implicitNotFound

import org.digimead.digi.lib.log.Logging

trait Loggable {
  @transient
  implicit lazy val log: RichLogger = try {
    Logging.getLogger(getClass)
  } catch {
    // allow to catch real exception cause
    case e: NoClassDefFoundError =>
      System.err.println(e)
      throw new RuntimeException("Unable to get logger for " + getClass.getName, e)
  }
}

object Loggable {
  object Where {
    val ALL = -1
    val HERE = -2
    val BEFORE = -3
  }
}