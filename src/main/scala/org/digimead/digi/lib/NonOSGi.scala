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

object NonOSGi extends Loggable {
  /** Start bundle. */
  def start() {
    //org.digimead.digi.lib.DependencyInjection(org.digimead.digi.lib.default)
    log.debug("Start Digi-Lib.")
    Logging.init()
    Caching.init()
  }
  /** Stop bundle. */
  def stop() {
    log.debug("Stop Digi-Lib.")
    Caching.shutdownHook.foreach(_())
    Caching.deinit()
    Logging.shutdownHook.foreach(_())
    Logging.deinit()
  }
}
