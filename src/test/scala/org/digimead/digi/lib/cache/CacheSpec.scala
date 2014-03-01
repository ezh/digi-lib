/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012-2013 Alexey Aksenov ezh@ezh.msk.ru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.digimead.digi.lib.cache

import org.digimead.digi.lib.DependencyInjection
import org.digimead.digi.lib.aop.{ Caching ⇒ AOPCaching }
import org.digimead.lib.test.LoggingHelper
import org.scalatest.ConfigMap
import org.scalatest.WordSpec
import org.scalatest.Matchers

import com.escalatesoft.subcut.inject.NewBindingModule

class CacheSpec000 extends CacheSpec.Base {
  "A Cache Singleton" should {
    "be persistent" in {
      val config = org.digimead.digi.lib.cache.default ~ org.digimead.digi.lib.default
      DependencyInjection(config)

      config.inject[Caching](None) should be theSameInstanceAs (config.inject[Caching](None))
      val caching1 = AOPCaching.inner
      val caching2 = AOPCaching.inner
      caching1 should be theSameInstanceAs (caching2)
      caching1.inner should be theSameInstanceAs (caching2.inner)
      caching1.ttl should equal(caching2.ttl)

      DependencyInjection(config ~ (NewBindingModule.newBindingModule(module ⇒ {})), false)
      val caching3 = AOPCaching.inner
      caching1 should be theSameInstanceAs (caching3)
      caching2 should be theSameInstanceAs (caching3)
    }
  }
}

class CacheSpec001 extends CacheSpec.Base {
  "A Cache Singleton" should {
    "create instance with default parameters" in {
      val config = org.digimead.digi.lib.cache.default ~ org.digimead.digi.lib.default
      DependencyInjection(config)
      val instance = AOPCaching.inner
      instance.inner should not be (null)
      instance.ttl should be(org.digimead.digi.lib.cache.default.inject[Long](Some("Cache.TTL")))
    }
  }
}

class CacheSpec002 extends CacheSpec.Base {
  "A Cache Singleton" should {
    "create instance with apropriate parameters" in {
      val innerCacheImplementation = new NilCache[String, Any]
      DependencyInjection(new NewBindingModule(module ⇒ {
        module.bind[Cache[String, Any]] identifiedBy "Cache.Engine" toSingle { innerCacheImplementation }
        module.bind[Long] identifiedBy "Cache.TTL" toSingle { 70L }
        module.bind[Caching] identifiedBy "Cache.Instance" toModuleSingle { implicit module ⇒ new Caching }
      }) ~ org.digimead.digi.lib.cache.default ~ org.digimead.digi.lib.default)
      val instance = AOPCaching.inner
      instance.ttl should be(70)
      instance.inner should be(innerCacheImplementation)
    }
  }
}

object CacheSpec {
  trait Base extends WordSpec with LoggingHelper with Matchers {
    override def beforeAll(configMap: ConfigMap) { adjustLoggingBeforeAll(configMap) }
  }
}
