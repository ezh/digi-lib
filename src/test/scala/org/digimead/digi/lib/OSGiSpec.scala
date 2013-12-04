/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012-2013 Alexey Aksenov ezh@ezh.msk.ru
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

import scala.collection.JavaConversions._

import org.digimead.digi.lib.log.api.Loggable
import org.digimead.lib.test.LoggingHelper
import org.digimead.lib.test.OSGiHelper
import org.mockito.Mockito
import org.osgi.service.log.LogService
import org.scalatest.ConfigMap
import org.scalatest.WordSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.slf4j.LoggerFactory

class OSGiSpec extends WordSpec with LoggingHelper with OSGiHelper with Matchers with MockitoSugar with Loggable {
  val testBundleClass = org.digimead.digi.lib.default.getClass()

  after {
    adjustOSGiAfter
    adjustLoggingAfter
  }
  before {
    DependencyInjection(org.digimead.digi.lib.default, false)
    adjustLoggingBefore
    adjustOSGiBefore
    osgiRegistry.foreach(_.start())
  }

  "OSGi framework" must {
    "running" in {
      val result = for {
        context <- osgiContext
        registry <- osgiRegistry
      } yield {
        val framework = context.getBundle()
        val bundles = context.getBundles()
        bundles should contain(framework)
        for (bundle <- bundles) {
          log.info("Bundle id: %s, symbolic name: %s, location: %s".
            format(bundle.getBundleId(), bundle.getSymbolicName(), bundle.getLocation()))
          val refs = bundle.getRegisteredServices()
          if (refs != null) {
            log.info("There are " + refs.length + " provided services")
            for (serviceReference <- refs) {
              for (key <- serviceReference.getPropertyKeys()) {
                log.info("\t" + key + " = " + (serviceReference.getProperty(key) match {
                  case arr: Array[_] => arr.mkString(",")
                  case n => n.toString
                }))
              }
              log.info("-----")
            }
          }
        }
      }
      assert(result.nonEmpty, "Unable to initialize OSGi framework.")
    }
  }
  "OSGi bundle" should {
    "start/stop" in {
      val result = for {
        context <- osgiContext
        registry <- osgiRegistry
        digiLib <- context.getBundles().find(_.getSymbolicName() == "org.digimead.digi.lib")
      } yield {
        digiLib.stop() // PojoSR is always starts bundles
        implicit val option = Mockito.atLeastOnce()
        withLogCaptor {
          digiLib.start()
        } { logCaptor =>
          logCaptor.getAllValues() find { entry =>
            entry.getMessage() == "Start Digi-Lib." &&
              entry.getLevel() == org.apache.log4j.Level.DEBUG
          } getOrElse { fail("Log message 'Start Digi-Lib.' at level DEBUG not found.") }
        }
        withLogCaptor {
          digiLib.stop()
        } { logCaptor =>
          logCaptor.getAllValues() find { entry =>
            entry.getMessage() == "Stop Digi-Lib." &&
              entry.getLevel() == org.apache.log4j.Level.DEBUG
          } getOrElse { fail("Log message 'Stop Digi-Lib.' at level DEBUG not found.") }
        }
      }
      assert(result.nonEmpty, "Unable to find DigiLib OSGi bundle.")
    }
    "interact with OSGi LogService" in {
      val result = for {
        context <- osgiContext
        registry <- osgiRegistry
        digiLib <- context.getBundles().find(_.getSymbolicName() == "org.digimead.digi.lib")
      } yield {
        val logServiceRef = context.getServiceReference(classOf[LogService])
        logServiceRef should not be (null)
        val logService = context.getService(logServiceRef)
        logService should not be (null)
        // multi threaded with org.apache.felix.log
        implicit val option = Mockito.timeout(1000)
        withLogCaptor {
          logService.log(LogService.LOG_INFO, "Test LogService from OSGiTest.")
        } { logCaptor =>
          logCaptor.getValue().getLevel() should be(org.apache.log4j.Level.INFO)
          logCaptor.getValue().getMessage() should be("Test LogService from OSGiTest.")
        }
        context.ungetService(logServiceRef)
      }
      assert(result.nonEmpty, "Unable to find DigiLib OSGi bundle.")
    }
  }

  override def beforeAll(configMap: ConfigMap) { adjustLoggingBeforeAll(configMap) }
}
