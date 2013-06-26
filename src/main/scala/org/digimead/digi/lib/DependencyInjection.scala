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

package org.digimead.digi.lib

import java.util.concurrent.atomic.AtomicBoolean

import scala.collection.mutable
import scala.ref.WeakReference

import org.osgi.framework.FrameworkUtil

import com.escalatesoft.subcut.inject.BindingModule

// We may reinitialize singleton with OSGi bundle reload.
/** Immutable dependency injection container */
object DependencyInjection extends api.DependencyInjection.Provider {
  private lazy val initializationRequired = new AtomicBoolean(true)

  /**
   * map of Class[PersistentInjectable]
   * It is impossible to use Class[PersistentInjectable] as key
   *  because it starts Scala object initialization
   */
  private lazy val injectables = new mutable.LinkedHashMap[String, WeakReference[api.DependencyInjection.PersistentInjectable]] with mutable.SynchronizedMap[String, WeakReference[api.DependencyInjection.PersistentInjectable]]

  /** Returns the current dependency injection content. */
  def apply(): BindingModule = synchronized {
    if (this.di == null)
      throw new IllegalStateException("Dependency injection is not initialized.")
    di
  }
  /**
   * Initialize the dependency injection framework.
   * @param di the dependency injection module.
   * @param stateValidator flag that require to throw IllegalStateException if DI is already initialized.
   */
  def apply(di: BindingModule, stateValidator: Boolean = true): Unit = synchronized {
    /*
     * Check if DI is already exists.
     */
    if (this.di != null)
      if (stateValidator)
        throw new IllegalStateException("Dependency injection is already initialized.")
      else
        return
    this.di = di // prevent for the situation with this.di == null
    inject()
  }
  def assertLazy[T: Manifest](name: Option[String], module: BindingModule) = Option(module).foreach { m =>
    val bindingKey = key[T](name)
    assert(m.bindings.isDefinedAt(bindingKey), s"$bindingKey not found")
    val bindingClassName = m.bindings(bindingKey).getClass.getName
    assert(bindingClassName.endsWith(".LazyModuleInstanceProvider") || bindingClassName.endsWith(".LazyInstanceProvider"),
      s"Unexpected binding provider for $bindingKey: $bindingClassName. Expect LazyInstanceProvider or LazyModuleInstanceProvider.")
  }
  def get(): Option[BindingModule] = synchronized { Option(di) }
  /** Inject DI into persistent objects. */
  def inject() {
    // initialize objects
    injectables.foreach {
      case (clazz, ref) =>
        ref.get match {
          case Some(pi) =>
            // initialize PersistentInjectable with assert
            assert(pi.bindingModule != null)
          case None =>
            try {
              val pi = Class.forName(clazz).getField("MODULE$").get(null).asInstanceOf[api.DependencyInjection.PersistentInjectable]
              // initialize PersistentInjectable with if
              if (pi.bindingModule != null)
                injectables(clazz) = new WeakReference(pi)
            } catch {
              case e: ClassNotFoundException =>
                val bundle = try { Option(FrameworkUtil.getBundle(getClass)) } catch { case e: Throwable => None }
                if (bundle.isEmpty) {
                  System.err.println("DependencyInjection error: " + e)
                  throw e
                }
              // This situation is on responsibility of library end user.
              // Why? We unable to cross bundle boundary and reach the specific class.
              // User must reinject DI from every bundle activator.
              // Initialized DI objects with val/lazy val will be unaffected.
              // Initialization will only affect newly created objects.
              case e: Throwable =>
                System.err.println("DependencyInjection error: " + e)
                throw e
            }
        }
    }
    // Call commit after initialization.
    // StashWithDependencyInjectionCommit is lazy val so it is called only once.
    injectables.foreach(clazz => getPersistentInjectable(clazz._1).map(invokeStashWithDependencyInjectionCommit))
  }
  def key[T](name: String)(implicit m: Manifest[T]) = com.escalatesoft.subcut.inject.getBindingKey[T](m, Some(name))
  def key[T](name: Option[String])(implicit m: Manifest[T]) = com.escalatesoft.subcut.inject.getBindingKey[T](m, name)
  def reset() = synchronized { di = null }
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
  def setPersistentInjectable(pobj: api.DependencyInjection.PersistentInjectable) =
    injectables(pobj.getClass.getName()) = new WeakReference(pobj)
  /**
   * Check if BindingModule contains illegal bindings
   * @param keyValidator f(x,y,z) that checks BindingModule key. DI may throws IllegalArgumentException if validator failed.
   *    example use case: OSGi environment. Pass only keys that begin with java.*, scala.* or defined globally or available
   * @return invalid keys
   */
  def validate(keyValidator: (Manifest[_], Option[String], Class[_]) => Boolean, instance: AnyRef) =
    apply().bindings.keys.filterNot { key => keyValidator(key.m, key.name, instance.getClass) }

  /**
   * get persistent object from injectables
   */
  private def getPersistentInjectable(persistentObjectClassName: String): Option[api.DependencyInjection.PersistentInjectable] =
    injectables.get(persistentObjectClassName) match {
      case Some(pobj) =>
        pobj.get orElse {
          try {
            val clazz = Class.forName(persistentObjectClassName).asInstanceOf[Class[api.DependencyInjection.PersistentInjectable]]
            val result = clazz.getField("MODULE$").get(clazz).asInstanceOf[api.DependencyInjection.PersistentInjectable]
            injectables(persistentObjectClassName) = new WeakReference(result)
            Some(result)
          } catch {
            // This catches all Throwables because we must return None if something wrong
            case e: Throwable =>
              None
          }
        }
      case None =>
        None
    }
}
