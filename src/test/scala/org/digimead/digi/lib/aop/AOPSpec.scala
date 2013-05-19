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
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.digimead.digi.lib.DependencyInjection
import org.digimead.digi.lib.log.Loggable

class AOPSpec extends FunSpec with BeforeAndAfterAll with ShouldMatchers {
  override def beforeAll() {
    org.apache.log4j.BasicConfigurator.resetConfiguration()
    org.apache.log4j.BasicConfigurator.configure()
    org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.TRACE)
  }
  describe("A Log instances") {
    it("should process annotated functions") {
      DependencyInjection.get.foreach(_ => DependencyInjection.clear)
      DependencyInjection.set(org.digimead.digi.lib.default)
      val test = new Logging.Basic
      test.log.debug(">>>>>>>>>")
      test.void(() => {})
      test.nonVoid(() => 1)
      try {
        test.void(() => throw new RuntimeException("test"))
      } catch {
        case e: RuntimeException =>
      }
      test.log.debug("<<<<<<<<<")
    }
  }
  describe("A Cache instances") {
    it("should process annotated functions") {
      DependencyInjection.get.foreach(_ => DependencyInjection.clear)
      DependencyInjection.set(org.digimead.digi.lib.default)
      val test = new Caching.Basic with Loggable
      test.log.debug(">>>>>>>>>")
      test.cached(() => Option(2))
      test.log.debug("<<<<<<<<<")
    }
  }
}

