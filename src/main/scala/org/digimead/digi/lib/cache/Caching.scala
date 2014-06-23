/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012-2014 Alexey Aksenov ezh@ezh.msk.ru
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

import akka.actor.{ ActorSystem, Props }
import akka.util.Timeout
import com.escalatesoft.subcut.inject.{ BindingModule, Injectable }
import java.util.concurrent.ConcurrentHashMap
import org.digimead.digi.lib.api.XDependencyInjection
import org.digimead.digi.lib.log.api.XLoggable
import scala.collection.JavaConverters.mapAsScalaMapConverter
import scala.concurrent.duration.DurationInt
import scala.language.{ implicitConversions, postfixOps }
import scala.ref.SoftReference

class Caching(implicit val bindingModule: BindingModule) extends Injectable with XLoggable {
  val inner = inject[Cache[String, Any]]("Cache.Engine")
  val requestTimeout = Timeout(1 seconds)
  val ttl = inject[Long]("Cache.TTL")
  // key -> (timestamp, data)
  private[cache] val map = new ConcurrentHashMap[String, SoftReference[(Long, Any)]].asScala
  log.debug("Alive.")

  val actor = Caching.actorSystem.actorOf(Props(new Actor()), Caching.id)

  def init() {
    log.debug("Initialize caching with %s.".format(this.toString))
  }
  def deinit() {
    log.debug("Deinitialize %s.".format(this.toString))
    val stopped = akka.pattern.Patterns.gracefulStop(Caching.inner.actor, 5 seconds)
    scala.concurrent.Await.result(stopped, 5 seconds)
  }
  override def toString() = "default Caching implementation"

  class Actor extends akka.actor.Actor {
    def receive = {
      case Caching.XMessage.Get(namespace, key, period) ⇒
        try {
          sender ! inner.get(namespace, key, period)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable ⇒
            log.warn(e.getMessage(), e)
            sender ! None
        }
      case Caching.XMessage.GetByID(namespaceID, key, period) ⇒
        try {
          sender ! inner.get(namespaceID, key, period)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable ⇒
            log.warn(e.getMessage(), e)
            sender ! None
        }
      case Caching.XMessage.Update(namespace, key, value) ⇒
        try {
          inner.update(namespace, key, value)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable ⇒
            log.warn(e.getMessage(), e)
        }
      case Caching.XMessage.UpdateByID(namespaceID, key, value) ⇒
        try {
          inner.update(namespaceID, key, value)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable ⇒
            log.warn(e.getMessage(), e)
        }
      case Caching.XMessage.UpdateMany(namespace, updates) ⇒
        try {
          inner.update(namespace, updates)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable ⇒
            log.warn(e.getMessage(), e)
        }
      case Caching.XMessage.Remove(namespace, key) ⇒
        try {
          sender ! inner.remove(namespace, key)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable ⇒
            log.warn(e.getMessage(), e)
        }
      case Caching.XMessage.RemoveByID(namespaceID, key) ⇒
        try {
          sender ! inner.remove(namespaceID, key)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable ⇒
            log.warn(e.getMessage(), e)
        }
      case Caching.XMessage.Clear(namespace) ⇒
        try {
          inner.clear(namespace)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable ⇒
            log.warn(e.getMessage(), e)
        }
      case message: AnyRef ⇒
        log.errorWhere("Skip unknown message %s: %s.".format(message.getClass.getName, message))
      case message ⇒
        log.errorWhere("Skip unknown message %s.".format(message))
    }
  }
}

object Caching {
  implicit def Caching2implementation(l: Caching.type): Caching = inner
  /** Actor id. */
  lazy val id = getClass.getName.dropRight(1)

  def actorSystem(): ActorSystem = DI.system
  def inner(): Caching = DI.implementation
  def shutdownHook() = DI.shutdownHook

  object XMessage {
    case class Get(namespace: scala.Enumeration#Value, key: String, ttl: Long = Caching.inner.ttl)
    case class GetByID(namespaceId: Int, key: String, ttl: Long = Caching.inner.ttl)
    case class Update(namespace: scala.Enumeration#Value, key: String, value: Any)
    case class UpdateByID(namespaceId: Int, key: String, value: Any)
    case class UpdateMany(namespace: scala.Enumeration#Value, updates: Iterable[(String, Any)])
    case class Remove(namespace: scala.Enumeration#Value, key: String)
    case class RemoveByID(namespaceId: Int, key: String)
    case class Clear(namespace: scala.Enumeration#Value)
  }
  /**
   * Dependency injection routines
   */
  private object DI extends XDependencyInjection.PersistentInjectable {
    /** Actor system DI cache */
    lazy val system = inject[ActorSystem]
    /** Caching implementation DI cache */
    lazy val implementation = inject[Caching]
    /** User defined shutdown hook */
    lazy val shutdownHook = injectOptional[() ⇒ Any]("Cache.ShutdownHook")
  }
}
