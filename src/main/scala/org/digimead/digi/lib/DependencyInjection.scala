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

import scala.collection.mutable
import scala.ref.WeakReference

import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.Injectable

object DependencyInjection {
  private var firstRun = true
  private var di: BindingModule = null
  /**
   * map of Class[PersistentInjectable]
   * It is impossible to use Class[PersistentInjectable] as key
   *  because it starts Scala object initialization
   */
  private val injectables = new mutable.HashMap[String, WeakReference[PersistentInjectable]] with mutable.SynchronizedMap[String, WeakReference[PersistentInjectable]]

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
      injectables.foreach { clazz => getPersistentInjectable(clazz._1).map(_.updateInjection) }
    injectionHook
    injectables.foreach { clazz => getPersistentInjectable(clazz._1).map(_.commitInjection) }
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
  def key[T](name: String)(implicit m: Manifest[T]) = com.escalatesoft.subcut.inject.getBindingKey[T](m, Some(name))
  def key[T](name: Option[String])(implicit m: Manifest[T]) = com.escalatesoft.subcut.inject.getBindingKey[T](m, name)
  /**
   * create wrapper for SubCut toModuleSingle
   * singleton will initialized only once
   */
  def makeInitOnce[T](f: (BindingModule) => T): BindingModule => T = {
    @volatile var saved: Option[T] = None
    (newModule) => saved getOrElse { saved = Some(f(newModule)); saved.get }
  }
  /**
   * add persistent object in injectables
   */
  def setPersistentInjectable(persistentObjectClassName: String) = {
    if (!injectables.contains(persistentObjectClassName))
      injectables(persistentObjectClassName) = new WeakReference(null)
  }
  /**
   * add/update persistent object in injectables
   */
  def setPersistentInjectable(pobj: PersistentInjectable) =
    injectables(pobj.getClass.getName()) = new WeakReference(pobj)
  /**
   * get persistent object from injectables
   */
  private def getPersistentInjectable(persistentObjectClassName: String): Option[PersistentInjectable] =
    injectables.get(persistentObjectClassName) match {
      case Some(pobj) =>
        pobj.get orElse {
          try {
            val clazz = Class.forName(persistentObjectClassName).asInstanceOf[Class[PersistentInjectable]]
            val result = clazz.getField("MODULE$").get(clazz).asInstanceOf[PersistentInjectable]
            injectables(persistentObjectClassName) = new WeakReference(result)
            Some(result)
          } catch {
            case e =>
              None
          }
        }
      case None =>
        None
    }
  trait PersistentInjectable extends Injectable {
    setPersistentInjectable(this)

    def commitInjection()
    def updateInjection()
  }
}
