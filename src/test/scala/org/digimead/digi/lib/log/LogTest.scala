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

import java.util.Date
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers._

class LoggingTestSimpleInit extends FunSuite with BeforeAndAfter {

  test("logging initialization via Record & Logging") {
    val thread = new Thread {
      override def run {
        val testClass = new Logging() {
          log.debug("hello")
        }
      }
    }
    thread.start
    Record.init(new Record.DefaultInit)
    Logging.init(new Logging.DefaultInit {
      override val loggers = Seq(ConsoleLogger)
    })
    thread.join()
    Logging.resume()
    Logging.addLogger(ConsoleLogger)
    Logging.delLogger(ConsoleLogger)
  }
}
