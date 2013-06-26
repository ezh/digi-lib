/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2013 Alexey Aksenov ezh@ezh.msk.ru
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

package org.digimead.digi.lib.api

import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.Injectable

/**
 * Dependency Injection service that contains actual DI module.
 */
trait DependencyInjection {
  /** Returns actual DI. From the nonOSGi world, for example. */
  def getDependencyInjection(): BindingModule
  /**
   * Returns DI key validator if any.
   * @return - f(x,y,z) where x is key manifest, y is key name, z is DI loader.
   */
  def getDependencyValidator(): Option[(Manifest[_], Option[String], Class[_]) => Boolean]
}

object DependencyInjection {
  /** Current application DI */
  private var di: BindingModule = null

  /** Provider interface. */
  trait Provider {
    /** Get DI module. */
    def di = DependencyInjection.di
    /** Set DI module. */
    def di_=(module: BindingModule) = DependencyInjection.di = module
    /** Call stashWithDependencyInjectionCommit inside PersistentInjectable. */
    protected def invokeStashWithDependencyInjectionCommit(obj: PersistentInjectable): Unit =
      obj.stashWithDependencyInjectionCommit
  }
  /** Singleton consumer interface. */
  trait PersistentInjectable extends Injectable {
    implicit def bindingModule = DependencyInjection.synchronized {
      if (DependencyInjection.di == null)
        throw new IllegalStateException("Dependency injection is not initialized.")
      DependencyInjection.di
    }
    /** Stash that is called only once. */
    private[DependencyInjection] lazy val stashWithDependencyInjectionCommit = { injectionCommit() }

    org.digimead.digi.lib.DependencyInjection.setPersistentInjectable(this)
    /**
     * Inject an instance for the given trait based on the class type required. If there is no binding, this
     * method will throw a BindingException. This form is for straight trait injection without an identifying name.
     * @return an instance configured by the binding module to use for the given trait.
     */
    override def inject[T <: Any](implicit m: scala.reflect.Manifest[T]): T =
      synchronized { super.inject[T] }

    /**
     * Inject an instance for the given trait based on the class type required and an ID symbol. If there is no
     * matching binding, this method will throw a BindingException. The Symbol provided will be converted to a string
     * prior to the lookup, so the symbol is interchangeable with the string version of the same ID, in other words
     * 'maxPoolSize and "maxPoolSize" are considered equivalent by the lookup mechanism.
     * @param symbol the identifying name to look up for the binding, e.g. 'maxPoolSize
     * @return an instance configured by the binding module to use for the given trait and ID
     */
    override def inject[T <: Any](symbol: Symbol)(implicit m: scala.reflect.Manifest[T]): T =
      synchronized { super.inject[T](symbol) }

    /**
     * Inject an instance for the given trait based on the class type required and an ID string. If there is no
     * matching binding, this method will throw a BindingException. The string ID is interchangeable with the
     * symbol version of the same ID, in other words 'maxPoolSize and "maxPoolSize" are considered equivalent by the
     * lookup mechanism.
     * @param name the identifying name to look up for the binding, e.g. "maxPoolSize"
     * @return an instance configured by the binding module to use for the given trait and ID
     */
    override def inject[T <: Any](name: String)(implicit m: scala.reflect.Manifest[T]): T =
      synchronized { super.inject[T](name) }

    /**
     * Inject an instance for the given trait based on the class type only if there is no instance already provided.
     * If no instance is provided (i.e. the existing impl passed in is null) and no binding is available to match, a
     * BindingException will be thrown. If an existing impl is provided (not null), then the binding will not be
     * used and does not need to be present. This form of the inject does not need a provided ID symbol or string.
     * @param implToUse from the call site. If it is null, the binding provider will fill it in instead
     * @return an instance configured by the binding module to use for the given trait
     */
    override def injectIfMissing[T <: Any](implToUse: Option[T])(implicit m: scala.reflect.Manifest[T]): T =
      synchronized { super.injectIfMissing[T](implToUse) }

    /**
     * Inject an instance for the given trait based on the class type only if there is no instance already provided.
     * If no instance is provided (i.e. the existing impl passed in is null) and no binding is available to match, a
     * BindingException will be thrown. If an existing impl is provided (not null), then the binding will not be
     * used and does not need to be present. This form of the inject takes a symbol ID to use to match the binding.
     * @param implToUse from the call site. If it is null, the binding provider will fill it in instead
     * @param name binding ID symbol to use - e.g. 'maxPoolSize
     * @return an instance configured by the binding module to use for the given trait
     */
    override def injectIfMissing[T <: Any](implToUse: Option[T], name: String)(implicit m: scala.reflect.Manifest[T]): T =
      synchronized { super.injectIfMissing[T](implToUse, name) }

    /**
     * Inject an instance for the given trait based on the class type only if there is no instance already provided.
     * If no instance is provided (i.e. the existing impl passed in is null) and no binding is available to match, a
     * BindingException will be thrown. If an existing impl is provided (not null), then the binding will not be
     * used and does not need to be present. This form of the inject takes a string ID to use to match the binding.
     * @param implToUse from the call site. If it is null, the binding provider will fill it in instead
     * @param symbol binding ID symbol to use - e.g. 'maxPoolSize
     * @return an instance configured by the binding module to use for the given trait
     */
    override def injectIfMissing[T <: Any](implToUse: Option[T], symbol: Symbol)(implicit m: scala.reflect.Manifest[T]): T =
      synchronized { super.injectIfMissing[T](implToUse, symbol) }

    /**
     * Inject an instance if a binding for that type is defined. If it is not defined, the function provided will
     * be used instead to create an instance to be used. This is arguably the most useful and efficient form of
     * injection usage, as the typical configuration can be provided at the call site and developers can easily
     * see what the "usual" instance is. An alternative binding will only be used if it is defined, e.g. for testing.
     * This form of the injector takes only a trait to match and no ID name.
     * @param fn a function to be used to return an instance, if there is no binding defined for the desired trait.
     * @return an instance that subclasses the trait, either from the binding definitions, or using the provided
     * function if no matching binding is defined.
     */
    override def injectIfBound[T <: Any](fn: => T)(implicit m: scala.reflect.Manifest[T]): T =
      synchronized { super.injectIfBound[T]({ fn }) }

    /**
     * Inject an instance if a binding for that type is defined. If it is not defined, the function provided will
     * be used instead to create an instance to be used. This is arguably the most useful and efficient form of
     * injection usage, as the typical configuration can be provided at the call site and developers can easily
     * see what the "usual" instance is. An alternative binding will only be used if it is defined, e.g. for testing.
     * This form of the injector takes a symbol ID to use in the binding definition lookup, e.g. 'maxPoolSize.
     * @param name symbol ID to be used to identify the matching binding definition.
     * @param fn a function to be used to return an instance, if there is no binding defined for the desired trait.
     * @return an instance that subclasses the trait, either from the binding definitions, or using the provided
     * function if no matching binding is defined.
     */
    override def injectIfBound[T <: Any](name: String)(fn: => T)(implicit m: scala.reflect.Manifest[T]): T =
      synchronized { super.injectIfBound[T](name)({ fn }) }

    /**
     * Inject an instance if a binding for that type is defined. If it is not defined, the function provided will
     * be used instead to create an instance to be used. This is arguably the most useful and efficient form of
     * injection usage, as the typical configuration can be provided at the call site and developers can easily
     * see what the "usual" instance is. An alternative binding will only be used if it is defined, e.g. for testing.
     * This form of the injector takes a string ID to use in the binding definition lookup, e.g. "maxPoolSize".
     * @param symbol ID to be used to identify the matching binding definition.
     * @param fn a function to be used to return an instance, if there is no binding defined for the desired trait.
     * @return an instance that subclasses the trait, either from the binding definitions, or using the provided
     * function if no matching binding is defined.
     */
    override def injectIfBound[T <: Any](symbol: Symbol)(fn: => T)(implicit m: scala.reflect.Manifest[T]): T =
      synchronized { super.injectIfBound[T](symbol)({ fn }) }

    /**
     * Inject an optional instance for the given trait based on the class type required. If there is no binding, this
     * method will return None. This form is for straight trait injection without an identifying name.
     * @return an optional instance configured by the binding module to use for the given trait.
     */
    override def injectOptional[T <: Any](implicit m: scala.reflect.Manifest[T]): Option[T] =
      synchronized { super.injectOptional[T] }

    /**
     * Inject an optional instance for the given trait based on the class type required and an ID symbol. If there is no
     * matching binding, this method will return None. The Symbol provided will be converted to a string
     * prior to the lookup, so the symbol is interchangeable with the string version of the same ID, in other words
     * 'maxPoolSize and "maxPoolSize" are considered equivalent by the lookup mechanism.
     * @param symbol the identifying name to look up for the binding, e.g. 'maxPoolSize
     * @return an optional instance configured by the binding module to use for the given trait and ID
     */
    override def injectOptional[T <: Any](symbol: Symbol)(implicit m: scala.reflect.Manifest[T]): Option[T] =
      synchronized { super.injectOptional[T](symbol) }

    /**
     * Inject an optional instance for the given trait based on the class type required and an ID string. If there is no
     * matching binding, this method will return None. The string ID is interchangeable with the
     * symbol version of the same ID, in other words 'maxPoolSize and "maxPoolSize" are considered equivalent by the
     * lookup mechanism.
     * @param name the identifying name to look up for the binding, e.g. "maxPoolSize"
     * @return an optional instance configured by the binding module to use for the given trait and ID
     */
    override def injectOptional[T <: Any](name: String)(implicit m: scala.reflect.Manifest[T]): Option[T] =
      synchronized { super.injectOptional[T](name) }

    def injectionCommit() {}
  }
}
