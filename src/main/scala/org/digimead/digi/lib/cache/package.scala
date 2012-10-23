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

import org.digimead.digi.lib.cache.Cache
import org.digimead.digi.lib.cache.Caching
import org.digimead.digi.lib.cache.NilCache
import org.scala_tools.subcut.inject.NewBindingModule

package object cache {
  lazy val default = new NewBindingModule(module => {
    module.bind[Cache[String, Any]] identifiedBy "Cache.Engine" toSingle { new NilCache[String, Any] }
    module.bind[Long] identifiedBy "Cache.TTL" toSingle { 1000 * 60 * 10L } // 10 minutes
    lazy val cachingSingleton = DependencyInjection.makeSingleton(implicit module => new Caching)
    module.bind[Caching] toModuleSingle { cachingSingleton(_) }
  })
}
