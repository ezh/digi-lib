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

import java.io.File

import scala.actors.Actor
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap
import scala.ref.SoftReference

import org.digimead.digi.lib.aop.Loggable
import org.digimead.digi.lib.log.Logging

trait CacheT[K, V] {
  def get(namespace: scala.Enumeration#Value, key: K): Option[V]
  def get(namespace: scala.Enumeration#Value, key: K, period: Long): Option[V]
  def get(namespaceID: Int, key: K, period: Long): Option[V]
  def apply(namespace: scala.Enumeration#Value, key: K): V
  def apply(namespace: scala.Enumeration#Value, key: K, period: Long): V
  def apply(namespaceID: Int, key: K, period: Long): V
  def update(namespace: scala.Enumeration#Value, key: K, value: V)
  def update(namespaceID: Int, key: K, value: V)
  def update(namespace: scala.Enumeration#Value, updates: Iterable[(K, V)])
  def remove(namespace: scala.Enumeration#Value, key: K): Option[V]
  def remove(namespaceID: Int, key: K): Option[V]
  def clear(namespace: scala.Enumeration#Value): Unit
}

class NilCache extends CacheT[String, Any] {
  def get(namespace: scala.Enumeration#Value, key: String) = None
  def get(namespace: scala.Enumeration#Value, key: String, period: Long) = None
  def get(namespaceID: Int, key: String, period: Long) = None
  def apply(namespace: scala.Enumeration#Value, key: String) = None
  def apply(namespace: scala.Enumeration#Value, key: String, period: Long) = None
  def apply(namespaceID: Int, key: String, period: Long) = None
  def update(namespace: scala.Enumeration#Value, key: String, value: Any): Unit = {}
  def update(namespaceID: Int, key: String, value: Any): Unit = {}
  def update(namespace: scala.Enumeration#Value, updates: Iterable[(String, Any)]): Unit = {}
  def remove(namespace: scala.Enumeration#Value, key: String): Option[Any] = None
  def remove(namespaceID: Int, key: String): Option[Any] = None
  def clear(namespace: scala.Enumeration#Value): Unit = {}
}

object Caching extends Logging {
  private var inner: CacheT[String, Any] = new NilCache
  private var period: Long = 1000 * 60 * 10 // 10 minutes
  private var cacheClass: String = ""
  private var cachePath: String = null
  private var cacheFolder: File = null
  // key -> (timestamp, data)
  private val map = new HashMap[String, SoftReference[(Long, Any)]]() with SynchronizedMap[String, SoftReference[(Long, Any)]]
  log.debug("alive")

  val actor = {
    val actor = new Actor {
      def act = {
        loop {
          react {
            case Message.Get(namespace, key, period) =>
              try {
                reply(inner.get(namespace, key, period))
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
                  reply(None)
              }
            case Message.GetByID(namespaceID, key, period) =>
              try {
                reply(inner.get(namespaceID, key, period))
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
                  reply(None)
              }
            case Message.Update(namespace, key, value) =>
              try {
                inner.update(namespace, key, value)
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Message.UpdateByID(namespaceID, key, value) =>
              try {
                inner.update(namespaceID, key, value)
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Message.UpdateMany(namespace, updates) =>
              try {
                inner.update(namespace, updates)
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Message.Remove(namespace, key) =>
              try {
                reply(inner.remove(namespace, key))
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Message.RemoveByID(namespaceID, key) =>
              try {
                reply(inner.remove(namespaceID, key))
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Message.Clear(namespace) =>
              try {
                inner.clear(namespace)
              } catch {
                case e =>
                  log.warn(e.getMessage(), e)
              }
            case Message.Reinitialize(arg) =>
              inner = null
              init(arg)
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

  def getDefaultPeriod(): Long = synchronized {
    period
  }
  @Loggable
  def setDefaultPeriod(_period: Long) = synchronized {
    period = _period
  }
  @Loggable
  def init(arg: Init): Unit = synchronized {
    if (inner != null) {
      /*
       * since actor is a single point of entry
       * process all requests
       * and then reinitialize
       */
      actor ! Message.Reinitialize(arg)
      return
    }
    inner = arg.inner
    period = arg.period
    cacheClass = arg.cacheClass
    cachePath = arg.cachePath
    cacheFolder = arg.cacheFolder
    inner = try {
      log.debug("create cache with implementation \"" + cacheClass + "\"")
      Class.forName(cacheClass).newInstance().asInstanceOf[CacheT[String, Any]]
    } catch {
      case e =>
        log.warn(e.getMessage(), e)
        new NilCache()
    }
    if (cacheFolder != null && !cacheFolder.exists)
      if (!cacheFolder.mkdirs) {
        log.fatal("cannot create directory: " + cacheFolder)
        inner = new NilCache()
      }
    log.info("set cache timeout to \"" + period + "\"")
    log.info("set cache directory to \"" + cachePath + "\"")
    log.info("set cache implementation to \"" + inner.getClass.getName() + "\"")
  }
  def deinit() = synchronized {
    if (inner != null)
      inner = null
  }
  def initialized = synchronized { inner != null }

  trait Init {
    val inner: CacheT[String, Any]
    val period: Long
    val cacheClass: String
    val cachePath: String
    val cacheFolder: File
  }
  object DefaultInit extends Init {
    val inner: CacheT[String, Any] = null
    val period: Long = 1000 * 60 * 10 // 10 minutes
    val cacheClass: String = "org.digimead.digi.lib.cache.NilCache"
    val cachePath: String = null
    val cacheFolder: File = null
  }
  object Message {
    case class Get(namespace: scala.Enumeration#Value, key: String, period: Long = getDefaultPeriod())
    case class GetByID(namespaceId: Int, key: String, period: Long = getDefaultPeriod())
    case class Update(namespace: scala.Enumeration#Value, key: String, value: Any)
    case class UpdateByID(namespaceId: Int, key: String, value: Any)
    case class UpdateMany(namespace: scala.Enumeration#Value, updates: Iterable[(String, Any)])
    case class Remove(namespace: scala.Enumeration#Value, key: String)
    case class RemoveByID(namespaceId: Int, key: String)
    case class Clear(namespace: scala.Enumeration#Value)
    case class Reinitialize(arg: Init)
  }
}
