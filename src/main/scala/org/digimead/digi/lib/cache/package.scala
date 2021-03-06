/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012-2014 Alexey Aksenov ezh@ezh.msk.ru
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

import com.escalatesoft.subcut.inject.NewBindingModule
import org.digimead.digi.lib.cache.{ Cache, Caching, NilCache }

package object cache {
  lazy val default = new NewBindingModule(module ⇒ {
    module.bind[Cache[String, Any]] identifiedBy "Cache.Engine" toSingle { new NilCache[String, Any] }
    module.bind[Long] identifiedBy "Cache.TTL" toSingle { 1000 * 60 * 10L } // 10 minutes
    module.bind[Caching] toModuleSingle { implicit module ⇒ new Caching }
  })
  DependencyInjection.setPersistentInjectable("org.digimead.digi.lib.cache.Caching$DI$")
  DependencyInjection.setPersistentInjectable("org.digimead.digi.lib.aop.Caching$DI$")
}
