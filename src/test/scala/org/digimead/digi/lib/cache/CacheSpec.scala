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
import org.digimead.digi.lib.aop.{ Caching => AOPCaching }
import org.scalatest.FunSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.ShouldMatchers

import com.escalatesoft.subcut.inject.NewBindingModule

class CacheSpec extends FunSpec with ShouldMatchers with PrivateMethodTester {
  org.apache.log4j.BasicConfigurator.resetConfiguration()
  org.apache.log4j.BasicConfigurator.configure()
  Caching

  describe("A Cache") {
    it("should have proper reinitialization") {
      DependencyInjection.get.foreach { _ => DependencyInjection.clear }
      val config = org.digimead.digi.lib.cache.default ~ org.digimead.digi.lib.default
      DependencyInjection.set(config)
      val privateInstance = PrivateMethod[Caching]('instance)

      config.inject[Caching](None) should be theSameInstanceAs (config.inject[Caching](None))
      val caching1 = AOPCaching invokePrivate privateInstance()
      DependencyInjection.reset()
      val caching2 = AOPCaching invokePrivate privateInstance()
      caching1 should be theSameInstanceAs (caching2)
      caching1.inner should be theSameInstanceAs (caching2.inner)
      caching1.ttl should equal(caching2.ttl)

      DependencyInjection.reset(config ~ (NewBindingModule.newBindingModule(module => {})))
      val caching3 = AOPCaching invokePrivate privateInstance()
      caching1 should not be theSameInstanceAs(caching3)
      caching2 should not be theSameInstanceAs(caching3)
    }
    it("should create singeton with default parameters") {
      DependencyInjection.get.foreach { _ => DependencyInjection.clear }
      val config = org.digimead.digi.lib.cache.default ~ org.digimead.digi.lib.default
      DependencyInjection.set(config)
      val privateInstance = PrivateMethod[Caching]('instance)
      val instance = AOPCaching invokePrivate privateInstance()
      instance.inner should not be (null)
      instance.ttl should be(org.digimead.digi.lib.cache.default.inject[Long](Some("Cache.TTL")))
    }
    it("should create singeton with apropriate parameters") {
      DependencyInjection.get.foreach { _ => DependencyInjection.clear }
      val innerCacheImplementation = new NilCache[String, Any]
      DependencyInjection.set(new NewBindingModule(module => {
        module.bind[Cache[String, Any]] identifiedBy "Cache.Engine" toSingle { innerCacheImplementation }
        module.bind[Long] identifiedBy "Cache.TTL" toSingle { 70L }
        module.bind[Caching] identifiedBy "Cache.Instance" toModuleSingle { implicit module => new Caching }
      }) ~ org.digimead.digi.lib.cache.default ~ org.digimead.digi.lib.default)
      val privateInstance = PrivateMethod[Caching]('instance)
      val instance = AOPCaching invokePrivate privateInstance()
      instance.inner should be(innerCacheImplementation)
      instance.ttl should be(70)
    }
  }
}
