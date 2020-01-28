package com.github.pdalpra.computerdb

import scala.concurrent.duration.FiniteDuration

import eu.timepit.refined.types.all._
import eu.timepit.refined.pureconfig._
import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

object Config {

  implicit val serverReader: ConfigReader[Server]                          = deriveReader
  implicit val restoreInitialReader: ConfigReader[Database.RestoreInitial] = deriveReader
  implicit val databaseReader: ConfigReader[Database]                      = deriveReader
  implicit val configReader: ConfigReader[Config]                          = deriveReader

  final case class Server(
      port: UserPortNumber,
      idleTimeout: FiniteDuration,
      threadPoolSize: PosInt
  )

  object Database {
    final case class RestoreInitial(
        enabled: Boolean,
        frequency: FiniteDuration
    )
  }
  final case class Database(
      url: String,
      poolSize: PosInt,
      username: String,
      password: String,
      restoreInitial: Database.RestoreInitial
  )
}

final case class Config(
    server: Config.Server,
    db: Config.Database
)
