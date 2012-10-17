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

import org.digimead.digi.lib.log.logger.RichLogger
import org.digimead.digi.lib.log.logger.RichLogger.rich2slf4j
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class MyLoggingInit extends Logging.DefaultInit {
  override def toString = "MyLoggingInit"
}

class LoggingTestSimpleInit extends FunSuite with BeforeAndAfter with ShouldMatchers {
  test("test Log4j binding over slf4j with RichLogger from Logging trait1") {
    LoggingInitializationArgument = Some(new MyLoggingInit)
    org.apache.log4j.BasicConfigurator.configure();
    class Test extends Logging {
      log.debug("hello")
    }
    val test = new Test
    test.log.___glance("start")
    test.log.isInstanceOf[RichLogger] should be(true)
    test.log.base.isInstanceOf[org.slf4j.impl.Log4jLoggerAdapter] should be(true)
  }
}

