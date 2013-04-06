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

import scala.language.implicitConversions

object NDC {
  implicit def tl2seq(tl: ThreadLocal[Seq[String]]): Seq[String] = Option(adapter.get).
    getOrElse { adapter.set(Seq[String]()); adapter.get }
  val adapter = new ThreadLocal[Seq[String]]

  def pop(): String = {
    val result = adapter.get().lastOption
    adapter.set(adapter.get().dropRight(1))
    result.getOrElse(null)
  }
  def push(value: String) = adapter.set(adapter.get() :+ value)
  def getSeq() = tl2seq(adapter)
}
