/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012 Alexey Aksenov ezh@ezh.msk.ru
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

package org.digimead.digi.lib.cache

import scala.actors.Actor
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import scala.ref.SoftReference

import org.digimead.digi.lib.DependencyInjection
import org.digimead.digi.lib.DependencyInjection.PersistentInjectable
import org.digimead.digi.lib.log.Loggable
import org.digimead.digi.lib.log.logger.RichLogger.rich2slf4j

import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.Injectable

class Caching(implicit val bindingModule: BindingModule) extends Injectable with Loggable {
  val inner = inject[Cache[String, Any]]("Cache.Engine")
  val ttl = inject[Long]("Cache.TTL")
  val shutdownHook = injectOptional[() => Any]("Cache.ShutdownHook")
  // key -> (timestamp, data)
  private[cache] val map = new HashMap[String, SoftReference[(Long, Any)]]() with SynchronizedMap[String, SoftReference[(Long, Any)]]
  log.debug("alive")

  val actor = {
    val actor = new Actor {
      def act = {
        loop {
          react {
            case Caching.Message.Get(namespace, key, period) =>
              try {
                reply(inner.get(namespace, key, period))
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
                  reply(None)
              }
            case Caching.Message.GetByID(namespaceID, key, period) =>
              try {
                reply(inner.get(namespaceID, key, period))
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
                  reply(None)
              }
            case Caching.Message.Update(namespace, key, value) =>
              try {
                inner.update(namespace, key, value)
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Caching.Message.UpdateByID(namespaceID, key, value) =>
              try {
                inner.update(namespaceID, key, value)
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Caching.Message.UpdateMany(namespace, updates) =>
              try {
                inner.update(namespace, updates)
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Caching.Message.Remove(namespace, key) =>
              try {
                reply(inner.remove(namespace, key))
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Caching.Message.RemoveByID(namespaceID, key) =>
              try {
                reply(inner.remove(namespaceID, key))
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Caching.Message.Clear(namespace) =>
              try {
                inner.clear(namespace)
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case message: AnyRef =>
              log.errorWhere("skip unknown message " + message.getClass.getName + ": " + message)
            case message =>
              log.errorWhere("skip unknown message " + message)
          }
        }
      }
    }
    actor.start()
    actor
  }

  def init() {
    log.debug("initialize caching with " + this.toString)
  }
  def deinit() {
    log.debug("deinitialize " + this.toString)
  }
  override def toString() = "default Caching implementation"
}

object Caching extends PersistentInjectable {
  implicit def bindingModule = DependencyInjection()
  @volatile private[cache] var instance = inject[Caching]
  Runtime.getRuntime().addShutdownHook(new Thread { override def run = Caching.instance.shutdownHook.foreach(_()) })

  def commitInjection() { instance.init }
  def updateInjection() {
    instance.deinit()
    instance = inject[Caching]
  }

  object Message {
    case class Get(namespace: scala.Enumeration#Value, key: String, ttl: Long = Caching.instance.ttl)
    case class GetByID(namespaceId: Int, key: String, ttl: Long = Caching.instance.ttl)
    case class Update(namespace: scala.Enumeration#Value, key: String, value: Any)
    case class UpdateByID(namespaceId: Int, key: String, value: Any)
    case class UpdateMany(namespace: scala.Enumeration#Value, updates: Iterable[(String, Any)])
    case class Remove(namespace: scala.Enumeration#Value, key: String)
    case class RemoveByID(namespaceId: Int, key: String)
    case class Clear(namespace: scala.Enumeration#Value)
  }
}
