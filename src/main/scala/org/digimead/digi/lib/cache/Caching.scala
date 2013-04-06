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

package org.digimead.digi.lib.cache

import java.util.concurrent.TimeUnit

import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.ref.SoftReference

import org.digimead.digi.lib.DependencyInjection
import org.digimead.digi.lib.DependencyInjection.PersistentInjectable
import org.digimead.digi.lib.log.Loggable
import org.digimead.digi.lib.log.logger.RichLogger.rich2slf4j

import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.Injectable

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.util.Timeout

import scala.language.postfixOps

class Caching(implicit val bindingModule: BindingModule) extends Injectable with Loggable {
  val inner = inject[Cache[String, Any]]("Cache.Engine")
  val requestTimeout = Timeout(1 seconds)
  val ttl = inject[Long]("Cache.TTL")
  val shutdownHook = injectOptional[() => Any]("Cache.ShutdownHook")
  // key -> (timestamp, data)
  private[cache] val map = new HashMap[String, SoftReference[(Long, Any)]]() with SynchronizedMap[String, SoftReference[(Long, Any)]]
  log.debug("alive")

  val actor = Caching.actorSystem.actorOf(Props(new Actor()))

  def init() {
    log.debug("initialize caching with " + this.toString)
  }
  def deinit() {
    log.debug("deinitialize " + this.toString)
    val stopped = akka.pattern.Patterns.gracefulStop(Caching.instance.actor, 5 seconds, Caching.actorSystem)
    scala.concurrent.Await.result(stopped, 5 seconds)
  }
  override def toString() = "default Caching implementation"

  class Actor extends akka.actor.Actor {
    def receive = {
      case Caching.Message.Get(namespace, key, period) =>
        try {
          sender ! inner.get(namespace, key, period)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable =>
            log.warn(e.getMessage(), e)
            sender ! None
        }
      case Caching.Message.GetByID(namespaceID, key, period) =>
        try {
          sender ! inner.get(namespaceID, key, period)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable =>
            log.warn(e.getMessage(), e)
            sender ! None
        }
      case Caching.Message.Update(namespace, key, value) =>
        try {
          inner.update(namespace, key, value)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable =>
            log.warn(e.getMessage(), e)
        }
      case Caching.Message.UpdateByID(namespaceID, key, value) =>
        try {
          inner.update(namespaceID, key, value)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable =>
            log.warn(e.getMessage(), e)
        }
      case Caching.Message.UpdateMany(namespace, updates) =>
        try {
          inner.update(namespace, updates)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable =>
            log.warn(e.getMessage(), e)
        }
      case Caching.Message.Remove(namespace, key) =>
        try {
          sender ! inner.remove(namespace, key)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable =>
            log.warn(e.getMessage(), e)
        }
      case Caching.Message.RemoveByID(namespaceID, key) =>
        try {
          sender ! inner.remove(namespaceID, key)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable =>
            log.warn(e.getMessage(), e)
        }
      case Caching.Message.Clear(namespace) =>
        try {
          inner.clear(namespace)
        } catch {
          // This catches all Throwables because there is no other error handler
          case e: Throwable =>
            log.warn(e.getMessage(), e)
        }
      case message: AnyRef =>
        log.errorWhere("skip unknown message " + message.getClass.getName + ": " + message)
      case message =>
        log.errorWhere("skip unknown message " + message)
    }
  }
}

object Caching extends PersistentInjectable {
  implicit def bindingModule = DependencyInjection()
  Runtime.getRuntime().addShutdownHook(new Thread {
    override def run = if (DependencyInjection.get.nonEmpty) Caching.injectOptional[Caching].foreach(_.shutdownHook.foreach(_()))
  })

  /*
   * dependency injection
   */
  def instance: Caching = inject[Caching]
  def actorSystem: ActorSystem = inject[ActorSystem]
  override def afterInjection(newModule: BindingModule) {
    instance.init
  }
  override def beforeInjection(newModule: BindingModule) {
    DependencyInjection.assertLazy[Caching](None, newModule)
    DependencyInjection.assertLazy[ActorSystem](None, newModule)
  }
  override def onClearInjection(oldModule: BindingModule) {
    instance.deinit()
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
