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

trait Cache[K, V] {
  def get(namespace: scala.Enumeration#Value, key: K): Option[V]
  def get(namespace: scala.Enumeration#Value, key: K, period: Long): Option[V]
  def get(namespaceID: Int, key: K, period: Long): Option[V]
  def apply(namespace: scala.Enumeration#Value, key: K): V
  def apply(namespace: scala.Enumeration#Value, key: K, period: Long): V
  def apply(namespaceID: Int, key: K, period: Long): V
  def update(namespace: scala.Enumeration#Value, key: K, value: V)
  def update(namespaceID: Int, key: K, value: V)
  def update(namespace: scala.Enumeration#Value, updates: Iterable[(K, V)])
  def remove(namespace: scala.Enumeration#Value, key: K): Option[V]
  def remove(namespaceID: Int, key: K): Option[V]
  def clear(namespace: scala.Enumeration#Value): Unit
}
