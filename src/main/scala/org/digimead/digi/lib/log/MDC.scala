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

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import org.slf4j.spi.MDCAdapter

class MDC extends MDCAdapter {
  def clear() = MDC.clear
  def get(key: String): String = MDC.get(key)
  def put(key: String, value: String) = MDC.put(key, value)
  def remove(key: String) = MDC.remove(key)
  def getCopyOfContextMap(): java.util.Map[_, _] = MDC.getCopyOfContextMap
  def setContextMap(contextMap: java.util.Map[_, _]) = MDC.setContextMap(contextMap)
}

object MDC extends MDCAdapter {
  implicit def tl2hash(tl: ThreadLocal[HashMap[String, String]]): HashMap[String, String] = Option(adapter.get).
    getOrElse { adapter.set(new HashMap[String, String]()); adapter.get }
  val adapter = new ThreadLocal[HashMap[String, String]]

  def clear() = adapter.clear
  def get(key: String): String = adapter.get(key).getOrElse(null)
  def put(key: String, value: String) = adapter(key) = value
  def remove(key: String) = adapter.remove(key)
  def getCopyOfContextMap(): java.util.Map[_, _] = tl2hash(adapter)
  def setContextMap(contextMap: java.util.Map[_, _]) {
    clear
    contextMap.keySet().foreach {
      key =>
        adapter(key.toString) = contextMap.get(key).toString
    }
  }
  def getSeq() = adapter.toSeq
}
