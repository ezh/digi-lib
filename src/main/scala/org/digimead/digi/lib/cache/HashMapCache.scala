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

package org.digimead.digi.lib.cache

import scala.ref.SoftReference

import org.digimead.digi.lib.log.api.Loggable

import scala.language.postfixOps

class HashMapCache extends Cache[String, Any] with Loggable {
  def get(namespace: scala.Enumeration#Value, key: String) =
    get(namespace, key, Caching.inner.ttl)
  def get(namespace: scala.Enumeration#Value, key: String, period: Long): Option[Any] =
    get(namespace.id, key, period)
  def get(namespaceID: Int, key: String, period: Long): Option[Any] = {
    log.trace("search cached value for namespace id " + namespaceID + " and key " + key)
    val ref = namespaceID + " " + key
    Caching.inner.map.get(ref).flatMap(_.get) match {
      case None =>
        log.trace("MISS")
        None
      case Some((time, obj)) =>
        if (period > 0)
          if (System.currentTimeMillis() - time <= period)
            Some(obj)
          else {
            // remove
            Caching.inner.map.remove(ref)
            None
          }
        else
          Some(obj)
    }
  }
  def apply(namespace: scala.Enumeration#Value, key: String) =
    get(namespace, key, Caching.inner.ttl) get
  def apply(namespace: scala.Enumeration#Value, key: String, period: Long): Any =
    get(namespace.id, key, period) get
  def apply(namespaceID: Int, key: String, period: Long): Any =
    get(namespaceID, key, period) get
  def update(namespace: scala.Enumeration#Value, key: String, value: Any): Unit =
    update(namespace.id, key, value)
  def update(namespaceID: Int, key: String, value: Any): Unit = {
    log.trace("update cached value for namespace id " + namespaceID + " and key " + key)
    val ref = namespaceID + " " + key
    Caching.inner.map(ref) = new SoftReference((System.currentTimeMillis(), value))
  }
  def update(namespace: scala.Enumeration#Value, updates: Iterable[(String, Any)]): Unit =
    updates.foreach(t => update(namespace, t._1, t._2))
  def remove(namespace: scala.Enumeration#Value, key: String): Option[Any] =
    remove(namespace.id, key)
  def remove(namespaceID: Int, key: String): Option[Any] = {
    val ref = namespaceID + " " + key
    Caching.inner.map.remove(ref).flatMap(ref => ref.get.map(t => t._2))
  }
  def clear(namespace: scala.Enumeration#Value): Unit = {
    val prefix = namespace.id + " "
    Caching.inner.map.filter(t => t._1.startsWith(prefix)).foreach(t => {
      log.debug("remove cache ref " + prefix + t._1)
      Caching.inner.map.remove(t._1)
    })
  }
}
