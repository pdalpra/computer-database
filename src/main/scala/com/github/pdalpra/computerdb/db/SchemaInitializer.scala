package com.github.pdalpra.computerdb.db

import cats.effect.Sync
import cats.syntax.all._
import doobie._
import doobie.implicits._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

trait SchemaInitializer[F[_]] {
  def initSchema: F[Unit]
}

object SchemaInitializer {

  def apply[F[_]: Sync](transactor: Transactor[F]): SchemaInitializer[F] =
    new SchemaInitializer[F] {
      private val logger = Slf4jLogger.getLogger[F]

      override def initSchema: F[Unit] = {
        val schemaTx = for {
          _ <- createSequences
          _ <- createCompanyTable
          _ <- createComputerTable
        } yield ()

        schemaTx.transact(transactor) *> logger.info("Database schema initialized")
      }

      private def createCompanyTable: ConnectionIO[Int] =
        sql"""create table company (
              id     bigint not null default company_seq.nextval,
              name   varchar(255) not null,

              constraint pk_company primary key (id)
            )
         """.update.run

      private def createComputerTable: ConnectionIO[Int] =
        sql"""create table computer (
              id             bigint not null default computer_seq.nextval,
              name           varchar(255) not null,
              introduced     timestamp,
              discontinued   timestamp,
              company_id     bigint,

              constraint pk_computer primary key (id),
              constraint fk_computer_company_1 foreign key (company_id) references company (id) on delete restrict on update restrict
            )
         """.update.run

      private def createSequences: doobie.ConnectionIO[Unit] =
        List(
          sql"""create sequence company_seq  start with 1""",
          sql"""create sequence computer_seq start with 1"""
        ).traverse_(_.update.run)
    }
}
