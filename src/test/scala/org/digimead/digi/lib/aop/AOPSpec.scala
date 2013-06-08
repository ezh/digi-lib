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

package org.digimead.digi.lib.aop

import scala.collection.JavaConversions._

import org.digimead.digi.lib.DependencyInjection
import org.digimead.digi.lib.log.api.Loggable
import org.digimead.lib.test.LoggingHelper
import org.mockito.Mockito
import org.scalatest.WordSpec
import org.scalatest.matchers.Matchers
import org.scalatest.matchers.ShouldMatchers

class AOPSpec extends WordSpec with LoggingHelper with ShouldMatchers {
  after { adjustLoggingAfter }
  before {
    DependencyInjection(org.digimead.digi.lib.default, false)
    adjustLoggingBefore
  }

  "A logging subsystem" should {
    "process annotated void function" in {
      val test = new LogBasic
      implicit val option = Mockito.times(2)
      withLogCaptor { test.void(() => {}) } { logCaptor =>
        val enter = logCaptor.getAllValues().head
        enter.getLevel() should be(org.apache.log4j.Level.TRACE)
        enter.getMessage.toString should endWith(" enteringMethod LogBasic::void")
        val leave = logCaptor.getAllValues().last
        leave.getLevel() should be(org.apache.log4j.Level.TRACE)
        leave.getMessage.toString should endWith(" leavingMethod LogBasic::void")
      }
    }
    "process annotated non void function" in {
      val test = new LogBasic
      implicit val option = Mockito.times(2)
      withLogCaptor { test.nonVoid(() => 1) } { logCaptor =>
        val enter = logCaptor.getAllValues().head
        enter.getLevel() should be(org.apache.log4j.Level.TRACE)
        enter.getMessage.toString should endWith(" enteringMethod LogBasic::nonVoid")
        val leave = logCaptor.getAllValues().last
        leave.getLevel() should be(org.apache.log4j.Level.TRACE)
        leave.getMessage.toString should endWith(" leavingMethod LogBasic::nonVoid result [1]")
      }
    }
    "process exception in annotated function" in {
      val test = new LogBasic
      implicit val option = Mockito.times(2)
      withLogCaptor {
        try {
          test.void(() => throw new RuntimeException("test"))
        } catch {
          case e: RuntimeException =>
        }
      } { logCaptor =>
        val enter = logCaptor.getAllValues().head
        enter.getLevel() should be(org.apache.log4j.Level.TRACE)
        enter.getMessage.toString should endWith(" enteringMethod LogBasic::void")
        val leave = logCaptor.getAllValues().last
        leave.getLevel() should be(org.apache.log4j.Level.TRACE)
        leave.getMessage.toString should endWith(" leavingMethodException LogBasic::void. Reason: test")
      }
    }
  }
  "A cache subsystem" should {
    "process annotated function" in {
      val test = new CacheBasic
      implicit val option = Mockito.times(4)
      withLogCaptor { test.cached(() => Option(2)) } { logCaptor =>
        val _1st = logCaptor.getAllValues()(0)
        _1st.getLevel() should be(org.apache.log4j.Level.DEBUG)
        _1st.getMessage.toString should be("Alive.")
        val _2nd = logCaptor.getAllValues()(1)
        _2nd.getLevel() should be(org.apache.log4j.Level.TRACE)
        _2nd.getMessage.toString should endWith(" with namespace id 0")
        val _3rd = logCaptor.getAllValues()(2)
        _3rd.getLevel() should be(org.apache.log4j.Level.TRACE)
        _3rd.getMessage.toString should endWith(" not found, invoking original method")
        val _4th = logCaptor.getAllValues()(3)
        _4th.getLevel() should be(org.apache.log4j.Level.TRACE)
        _4th.getMessage.toString should endWith(" updated")
      }
    }
  }

  override def beforeAll(configMap: Map[String, Any]) { adjustLoggingBeforeAll(configMap) }

  /** Stub class for @log annotation testing. */
  class LogBasic extends Loggable {
    @log
    def void[T](f: () => T) { f() }
    @log
    def nonVoid[T](f: () => T) = f()
  }
  /** Stub class for @cache annotation testing. */
  class CacheBasic extends Loggable {
    @cache
    def cached[T](f: () => T) = f()
  }
}

