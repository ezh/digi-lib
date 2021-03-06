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

import com.escalatesoft.subcut.inject.NewBindingModule
import akka.actor.ActorSystem

package org.digimead.digi {
  package object lib {
    lazy val default = log.default ~ cache.default ~ new NewBindingModule(module => {
      module.bind[ActorSystem] toSingle { ActorSystem("DigiSystem") }
    })
    lazy val defaultWithDC = log.defaultWithDC ~ cache.default
  }
}

package com.escalatesoft.subcut.inject {
  object getBindingKey {
    def apply[T](m: Manifest[T], name: Option[String]) = BindingKey(m, name)
  }
}
