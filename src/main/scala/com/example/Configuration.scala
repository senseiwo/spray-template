package com.example

import com.typesafe.config.ConfigFactory

import scala.util.Try

/**
 * Created by tomek on 5/27/15.
 */
trait Configuration {
  val cfg = ConfigFactory.load()
  lazy val host = Try(cfg.getString("service.host")).getOrElse("localhost")
  lazy val port = Try(cfg.getString("service.port")).getOrElse("8080")
}
