/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2013 Alexey Aksenov ezh@ezh.msk.ru
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

import org.digimead.digi.lib.cache.Caching
import org.digimead.digi.lib.cache.Caching.Caching2implementation
import org.digimead.digi.lib.log.Logging
import org.digimead.digi.lib.log.Logging.Logging2implementation
import org.digimead.digi.lib.log.api.Loggable
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.service.log.LogEntry
import org.osgi.service.log.LogListener
import org.osgi.service.log.LogReaderService
import org.osgi.service.log.LogService
import org.osgi.util.tracker.ServiceTracker
import org.osgi.util.tracker.ServiceTrackerCustomizer

class Activator extends BundleActivator with LogListener with ServiceTrackerCustomizer[LogReaderService, LogReaderService] with Loggable {
  @volatile protected var context: Option[BundleContext] = None
  @volatile protected var logReaderTracker: Option[ServiceTracker[LogReaderService, LogReaderService]] = None

  /** Start bundle. */
  def start(context: BundleContext) {
    this.context = Some(context)
    Option(context.getServiceReference(classOf[api.DependencyInjection])).
      map { currencyServiceRef ⇒ (currencyServiceRef, context.getService(currencyServiceRef)) } match {
        case Some((reference, diService)) ⇒
          // DI is already initialized somewhere so logging and caching must be too
          log.debug("Start Digi-Lib. Reinject DI.")
          DependencyInjection.reset()
          DependencyInjection(diService.getDependencyInjection)
          diService.getDependencyValidator.foreach { validator ⇒
            val invalid = DependencyInjection.validate(validator, this)
            if (invalid.nonEmpty)
              throw new IllegalArgumentException("Illegal DI keys found: " + invalid.mkString(","))
          }
          context.ungetService(reference)
        case None ⇒
          // DI must be initialized somewhere but we must initialize logging and caching
          log.debug("Start Digi-Lib.")
          val logReaderTracker = new ServiceTracker[LogReaderService, LogReaderService](context, classOf[LogReaderService].getName(), this)
          logReaderTracker.open()
          this.logReaderTracker = Some(logReaderTracker)
          Logging.init()
          Caching.init()
      }

  }
  /** Stop bundle. */
  def stop(context: BundleContext) {
    log.debug("Stop Digi-Lib.")
    this.logReaderTracker.foreach { tracker ⇒
      Caching.shutdownHook.foreach(_())
      Caching.deinit()
      Logging.shutdownHook.foreach(_())
      Logging.deinit()
      tracker.close()
    }
    this.logReaderTracker = None
    this.context = None
  }
  /** Transfer logEntry to log. */
  def logged(logEntry: LogEntry) {
    val bundle = logEntry.getBundle()
    val symbolicName = bundle.getSymbolicName().replaceAll("-", "_")
    val log = Logging.getLogger("osgi.logging." + symbolicName)
    val message = Option(logEntry.getServiceReference()) match {
      case Some(serviceReference) ⇒ logEntry.getMessage() + serviceReference.toString()
      case None ⇒ logEntry.getMessage()
    }
    logEntry.getLevel() match {
      case LogService.LOG_DEBUG ⇒ log.debug(message, logEntry.getException())
      case LogService.LOG_INFO ⇒ log.info(message, logEntry.getException())
      case LogService.LOG_WARNING ⇒ log.warn(message, logEntry.getException())
      case LogService.LOG_ERROR ⇒ log.error(message, logEntry.getException())
    }
  }
  /** Subscribe to new LogReaderService */
  def addingService(serviceReference: ServiceReference[LogReaderService]): LogReaderService = {
    context.map { context ⇒
      val logReaderService = context.getService(serviceReference)
      log.debug("Subscribe log listener to " + logReaderService.getClass.getName)
      logReaderService.addLogListener(this)
      logReaderService
    } getOrElse null
  }
  def modifiedService(serviceReference: ServiceReference[LogReaderService], logReaderService: LogReaderService) {}
  /** Unsubscribe from disposed LogReaderService */
  def removedService(serviceReference: ServiceReference[LogReaderService], logReaderService: LogReaderService) {
    log.debug("Unsubscribe log listener from " + logReaderService.getClass.getName)
    logReaderService.removeLogListener(this)
  }
}

object Activator extends Loggable {
  /** Non OSGi start. */
  def start() {
    //org.digimead.digi.lib.DependencyInjection(org.digimead.digi.lib.default)
    log.debug("Start Digi-Lib.")
    Logging.init()
    Caching.init()
  }
  /** Non OSGi stop. */
  def stop() {
    log.debug("Stop Digi-Lib.")
    Caching.shutdownHook.foreach(_())
    Caching.deinit()
    Logging.shutdownHook.foreach(_())
    Logging.deinit()
    if (!Caching.actorSystem.isTerminated) try {
      Caching.actorSystem.shutdown()
      Caching.actorSystem.awaitTermination()
    } catch {
      case e: Throwable ⇒
    }
  }
}
