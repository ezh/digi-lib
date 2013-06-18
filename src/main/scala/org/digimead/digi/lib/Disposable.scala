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

package org.digimead.digi.lib

import java.lang.ref.WeakReference

/** Disposable interface. */
trait Disposable {
  /** Disposable marker that points to manager. */
  val disposeable: Disposable.Marker

  /** Dispose instance. It should nullify all fields via reflection. */
  def dispose() {}
}

object Disposable {
  /** Wrap manager with weak reference for implicit value. */
  def apply(manager: Manager) = new WeakReference(manager)
  /** Register disposable at manager. */
  def apply(disposable: Disposable, manager: Manager): Marker = {
    manager.register(disposable)
    new Marker(new WeakReference(manager))
  }

  /** Manager interface. */
  trait Manager {
    /** Register the disposable instance. */
    def register(disposable: Disposable)
  }
  /** Disposable marker. */
  class Marker private[Disposable] (val manager: WeakReference[Manager])
}
