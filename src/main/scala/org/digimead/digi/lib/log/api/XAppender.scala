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

package org.digimead.digi.lib.log.api

trait XAppender {
  protected var f: (Array[XMessage]) => Unit
  def init() {}
  def apply(r: Array[XMessage]) = f(r)
  def deinit() {}
  def flush() {}
  def getF() = synchronized { f }
  def setF(_f: (Array[XMessage]) => Unit) = synchronized { f = _f }
}
