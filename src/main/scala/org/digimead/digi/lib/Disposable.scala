/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2013-2014 Alexey Aksenov ezh@ezh.msk.ru
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

import java.lang.ref.WeakReference
import org.digimead.digi.lib.aop.{ log ⇒ alog }
import org.digimead.digi.lib.log.api.XLoggable

/** Disposable interface. */
trait Disposable {
  /** Disposable marker that points to manager. */
  protected val disposeable: Disposable.Marker

  /** Dispose method. It should nullify all fields via reflection. */
  protected def dispose()
}

object Disposable {
  /** Wrap manager with weak reference for implicit value. */
  def apply(manager: Manager) = new WeakReference(manager)
  /** Register disposable at manager. */
  def apply(disposable: Disposable, manager: Manager): Marker = {
    manager.register(disposable)
    new Marker(new WeakReference(manager))
  }

  /** Clean method that nullify all obj non primitive fields via reflection. */
  def clean(obj: AnyRef): Unit =
    obj.getClass().getDeclaredFields().foreach(field ⇒
      if (!field.getType().isPrimitive()) try {
        if (!field.isAccessible())
          field.setAccessible(true)
        field.set(obj, null)
      } catch {
        case e: Throwable ⇒
          obj match {
            case loggable: XLoggable ⇒ try {
              loggable.log.debug(s"Unable to clear field ${field} for class ${obj.getClass()}: " + e.getMessage)
            } catch {
              case e: Throwable ⇒ // not interesting
            }
            case other ⇒ // not interesting
          }
      })
  /** Transitive disposable trait that is based on "Scala's Stackable Trait Pattern" */
  trait Transitive extends Disposable {
    /** Dispose method. It should nullify all fields via reflection. */
    abstract override protected def dispose() {
      super.dispose
      clean(this)
    }
  }
  /** Default disposable trait */
  trait Default extends Disposable {
    /** Stackable dispose method. */
    protected def dispose() {
      clean(this)
    }
  }
  /** Manager interface. */
  trait Manager {
    /** Call protected dispose method. */
    @alog
    def callDispose(disposable: Disposable) =
      disposable.dispose
    /** Register the disposable instance. */
    def register(disposable: Disposable)
  }
  /** Disposable marker. */
  class Marker private[Disposable] (val manager: WeakReference[Manager])
}
