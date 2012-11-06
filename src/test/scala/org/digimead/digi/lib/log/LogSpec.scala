/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012 Alexey Aksenov ezh@ezh.msk.ru
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

package org.digimead.digi.lib.log

import scala.annotation.implicitNotFound

import org.digimead.digi.lib.DependencyInjection
import org.digimead.digi.lib.log.logger.RichLogger
import org.digimead.digi.lib.log.logger.RichLogger.rich2slf4j
import org.scalatest.FunSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.ShouldMatchers

import com.escalatesoft.subcut.inject.NewBindingModule

class LogSpec extends FunSpec with ShouldMatchers with PrivateMethodTester {
  org.apache.log4j.BasicConfigurator.resetConfiguration()
  org.apache.log4j.BasicConfigurator.configure()

  describe("A Log") {
    it("should have proper reinitialization") {
      DependencyInjection.get.foreach(_ => DependencyInjection.clear)
      val config = org.digimead.digi.lib.cache.default ~ org.digimead.digi.lib.default
      DependencyInjection.set(config)
      val privateInstance = PrivateMethod[Logging]('instance)

      config.inject[Logging](None) should be theSameInstanceAs (config.inject[Logging](None))
      val logging1 = Logging invokePrivate privateInstance()
      DependencyInjection.reset()
      val logging2 = Logging invokePrivate privateInstance()
      logging1 should be theSameInstanceAs (logging2)
      logging1.record should be theSameInstanceAs (logging2.record)
      logging1.record.dateFormat should be theSameInstanceAs (logging2.record.dateFormat)
      logging1 should be theSameInstanceAs (config.inject[Logging](None))
      logging1.builder should not be theSameInstanceAs(config.inject[(String) => RichLogger](Some("Log.Builder")))

      DependencyInjection.reset(config ~ (NewBindingModule.newBindingModule(module => {})))
      val logging3 = Logging invokePrivate privateInstance()
      logging1 should not be theSameInstanceAs(logging3)
      logging2 should not be theSameInstanceAs(logging3)
    }
    it("should create singeton with default parameters") {
      DependencyInjection.get.foreach(_ => DependencyInjection.clear)
      val config = org.digimead.digi.lib.log.default ~ org.digimead.digi.lib.default
      DependencyInjection.set(config)
      val privateInstance = PrivateMethod[Logging]('instance)
      val instance = Logging invokePrivate privateInstance()
      instance.record should not be (null)
      instance.builder should not be (null)
      instance.isTraceWhereEnabled should be(false)
      instance.bufferedThread should be(None)
      instance.bufferedFlushLimit should be(1000)
      instance.shutdownHook should be(None)
      instance.bufferedAppender should be('empty)
      instance.richLogger.size should not be ('empty)
      instance.commonLogger should not be (null)

      class Test extends Loggable {
        log.debug("hello")
      }
      val test = new Test
      test.log.___glance("start")
      test.log.isInstanceOf[RichLogger] should be(true)
      test.log.base.isInstanceOf[org.slf4j.impl.Log4jLoggerAdapter] should be(true)
    }
    it("should create singeton with custom parameters") {
      DependencyInjection.get.foreach(_ => DependencyInjection.clear)
      val config1 = org.digimead.digi.lib.log.default ~ org.digimead.digi.lib.default
      val config2 = new NewBindingModule(module => {
        module.bind[Boolean] identifiedBy "Log.TraceWhereEnabled" toSingle { true }
      })
      val config = config2 ~ config1
      config.inject[Boolean](Some("Log.TraceWhereEnabled")) should be(true)
      DependencyInjection.set(config)
      val privateInstance = PrivateMethod[Logging]('instance)
      val instance = Logging invokePrivate privateInstance()
      instance.isTraceWhereEnabled should be(true)
    }
    it("should call deinit on reinitialization") {
      DependencyInjection.get.foreach(_ => DependencyInjection.clear)
      @volatile var deinitCall = false
      val config1 = org.digimead.digi.lib.default
      val config2 = new NewBindingModule(module => {
        module.bind[Logging] toModuleSingle { implicit module =>
          new Logging {
            override def deinit() {
              deinitCall = true
              super.deinit()
            }
          }
        }
      })
      deinitCall should be(false)
      // set
      DependencyInjection.set(config2 ~ config1)
      // clear
      DependencyInjection.get.foreach(_ => DependencyInjection.clear)
      deinitCall should be(false)
      // deinit
      DependencyInjection.set(config2 ~ config1)
      deinitCall should be(true)
    }
  }
}
