package com.github.pdalpra.computerdb.db.sql

import java.util.UUID

import com.github.pdalpra.computerdb.ComputerDatabaseSpec

import cats.effect.Resource
import doobie.h2.H2Transactor
import doobie.util.ExecutionContexts
import zio._
import zio.interop.catz._

abstract class SqlRepositorySpec extends ComputerDatabaseSpec {
  private def databaseUrl = s"jdbc:h2:mem:${UUID.randomUUID()};DB_CLOSE_DELAY=-1"

  protected def testTransactor: Resource[Task, H2Transactor[Task]] =
    for {
      connectionEC  <- ExecutionContexts.cachedThreadPool[Task]
      rawTransactor = H2Transactor.newH2Transactor[Task](databaseUrl, "sa", "", connectionEC, blocker)
      transactor    <- rawTransactor.evalTap(tx => SchemaInitializer(tx).initSchema)
    } yield transactor

}
