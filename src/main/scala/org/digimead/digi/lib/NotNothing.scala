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

/**
 * Trait for NotNothing compile time checking.
 */
sealed trait NotNothing[A] {
  type B
}

/**
 * Based on idea of Brendan W. McAdams (Evil Monkey Labs).
 * Trigger the ambiguity compilation error when explicit type is not supplied.
 * Usage def a[A: NotNothing](...
 */
object NotNothing {
  implicit val nothing = new NotNothing[Nothing] { type B = Any }
  implicit def notNothing[A] = new NotNothing[A] { type B = A }
}
