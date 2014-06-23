/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012-2014 Alexey Aksenov ezh@ezh.msk.ru
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

import com.escalatesoft.subcut.inject.NewBindingModule
import org.digimead.digi.lib.DependencyInjection
import org.digimead.digi.lib.log.api.{ XLoggable, XRichLogger }
import org.digimead.digi.lib.log.logger.RichLogger
import org.digimead.lib.test.LoggingHelper
import org.scalatest.{ ConfigMap, FunSpec, Matchers, PrivateMethodTester, WordSpec }
import scala.annotation.implicitNotFound

class LogSpec000 extends LogSpec.Base {
  "A Log Singleton" should {
    "be persistent" in {
      val config = org.digimead.digi.lib.cache.default ~ org.digimead.digi.lib.default
      DependencyInjection(config)

      config.inject[Logging](None) should be theSameInstanceAs (config.inject[Logging](None))
      val logging1 = Logging inner ()
      val logging2 = Logging inner ()
      logging1 should be theSameInstanceAs (logging2)
      logging1.record should be theSameInstanceAs (logging2.record)
      logging1.record.dateFormat should be theSameInstanceAs (logging2.record.dateFormat)
      logging1 should be theSameInstanceAs (config.inject[Logging](None))
      logging1.builder should not be theSameInstanceAs(config.inject[(String, Class[_]) ⇒ XRichLogger](Some("Log.Builder")))

      DependencyInjection(config ~ (NewBindingModule.newBindingModule(module ⇒ {})), false)
      val logging3 = Logging inner ()
      logging1 should be theSameInstanceAs (logging3)
      logging2 should be theSameInstanceAs (logging3)
    }
  }
}

class LogSpec001 extends LogSpec.Base {
  "A Log Singleton" should {
    "create instance with default parameters" in {
      val config = org.digimead.digi.lib.log.default ~ org.digimead.digi.lib.default
      DependencyInjection(config)
      val instance = Logging inner ()
      instance.record should not be (null)
      instance.builder should not be (null)
      instance.isWhereEnabled should be(false)
      instance.bufferedThread should be(None)
      instance.bufferedFlushLimit should be(1000)
      instance.shutdownHook should be(None)
      instance.bufferedAppender should be('empty)
      instance.richLogger should be('empty)
      instance.commonLogger should not be (null)

      class Test extends XLoggable {
        log.debug("hello")
      }
      val test = new Test
      test.log.___glance("start")
      instance.richLogger should not be ('empty)
      test.log.isInstanceOf[RichLogger] should be(true)
      test.log.base.isInstanceOf[org.slf4j.impl.Log4jLoggerAdapter] should be(true)
    }
  }
}

class LogSpec002 extends LogSpec.Base {
  "A Log Singleton" should {
    "create instance with custom parameters" in {
      val config1 = org.digimead.digi.lib.log.default ~ org.digimead.digi.lib.default
      val config2 = new NewBindingModule(module ⇒ {
        module.bind[Boolean] identifiedBy "Log.TraceWhereEnabled" toSingle { true }
      })
      val config = config2 ~ config1
      config.inject[Boolean](Some("Log.TraceWhereEnabled")) should be(true)
      DependencyInjection(config)
      val instance = Logging inner ()
      instance.isWhereEnabled should be(true)
    }
  }
}

object LogSpec {
  trait Base extends WordSpec with LoggingHelper with Matchers {
    override def beforeAll(configMap: ConfigMap) { adjustLoggingBeforeAll(configMap) }
  }
}
