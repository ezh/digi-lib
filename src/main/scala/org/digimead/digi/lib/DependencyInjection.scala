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

package org.digimead.digi.lib

import scala.collection.mutable.SynchronizedMap
import scala.collection.mutable.WeakHashMap
import scala.ref.WeakReference

import org.scala_tools.subcut.inject.BindingModule
import org.scala_tools.subcut.inject.Injectable

object DependencyInjection {
  val injectables = new WeakHashMap[AnyRef, WeakReference[PersistentInjectable]] with SynchronizedMap[AnyRef, WeakReference[PersistentInjectable]]
  private var di: BindingModule = null
  def apply(): BindingModule = synchronized {
    assert(di != null, "dependency injection not initialized")
    di
  }
  def set(di: BindingModule): BindingModule = synchronized {
    assert(this.di == null, "dependency injection already initialized")
    this.di = di
    // We set BindingModule before any Injectable created at the beginning, so injectables map is empty
    injectables.foreach(_._2.get.foreach(_.reloadInjection))
    di
  }
  def clear(): BindingModule = synchronized {
    assert(di != null, "dependency injection not initialized")
    val result = this.di
    this.di = null
    result
  }
  def get(): Option[BindingModule] = synchronized { Option(di) }
  def key[T](name: String)(implicit m: Manifest[T]) = org.scala_tools.subcut.inject.getBindingKey[T](m, Some(name))
  def key[T](name: Option[String])(implicit m: Manifest[T]) = org.scala_tools.subcut.inject.getBindingKey[T](m, name)
  trait PersistentInjectable extends Injectable {
    DependencyInjection.injectables(this) = new WeakReference(this)
    def reloadInjection()
  }
}
