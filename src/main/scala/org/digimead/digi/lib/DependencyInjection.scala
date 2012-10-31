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

import scala.ref.WeakReference

import org.scala_tools.subcut.inject.BindingModule
import org.scala_tools.subcut.inject.Injectable

object DependencyInjection {
  private var firstRun = true
  private var di: BindingModule = null
  private var injectables = Seq[WeakReference[PersistentInjectable]]()

  def apply(): BindingModule = synchronized {
    assert(di != null, "dependency injection not initialized")
    di
  }
  def set(di: BindingModule, injectionHook: => Unit = {}): BindingModule = synchronized {
    assert(this.di == null, "dependency injection already initialized")
    this.di = di
    // We set BindingModule before any Injectable created at the beginning, so injectables map is empty
    if (firstRun)
      firstRun = false
    else
      injectables.foreach(_.get.map(_.updateInjection))
    injectionHook
    injectables.foreach(_.get.map(_.commitInjection))
    di
  }
  def clear(): BindingModule = synchronized {
    assert(di != null, "dependency injection not initialized")
    val result = this.di
    this.di = null
    result
  }
  def reset(config: BindingModule = di) = Option(config).foreach(config => { Option(di).foreach(_ => clear); set(config) })
  def get(): Option[BindingModule] = synchronized { Option(di) }
  def key[T](name: String)(implicit m: Manifest[T]) = org.scala_tools.subcut.inject.getBindingKey[T](m, Some(name))
  def key[T](name: Option[String])(implicit m: Manifest[T]) = org.scala_tools.subcut.inject.getBindingKey[T](m, name)
  /**
   * create wrapper for SubCut toModuleSingle
   * if fixed is true - singleton will initialized only once
   * if fixed is false - singleton will reinitialized if module changed
   */
  def makeSingleton[T](f: (BindingModule) => T, fixed: Boolean = false): BindingModule => T = {
    @volatile var savedModule = new WeakReference[BindingModule](null)
    @volatile var saved: T = null.asInstanceOf[T]
    (newModule) =>
      if ((!fixed || saved == null) && savedModule.get != Some(newModule)) {
        savedModule = new WeakReference[BindingModule](newModule)
        saved = f(newModule)
        saved
      } else saved
  }
  private def registerInjectable(obj: PersistentInjectable) = synchronized {
    injectables = injectables :+ new WeakReference(obj)
  }
  trait PersistentInjectable extends Injectable {
    DependencyInjection.registerInjectable(this)

    def commitInjection()
    def updateInjection()
  }
}
