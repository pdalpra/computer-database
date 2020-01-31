package com.github.pdalpra.computerdb.db.sql

import com.github.pdalpra.computerdb.db.CompanyRepository
import com.github.pdalpra.computerdb.db.sql.DoobieInstances._
import com.github.pdalpra.computerdb.model._

import cats.effect.Sync
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.refined.implicits._

class SqlCompanyRepository[F[_]: Sync](transactor: Transactor[F]) extends CompanyRepository[F] {

  override def fetchAll: F[List[Company]] =
    sql"select * from company order by id"
      .query[Company]
      .to[List]
      .transact(transactor)

  override def loadAll(companies: List[NonEmptyString]): F[Unit] =
    Update[NonEmptyString]("insert into company (name) values (?)")
      .updateMany(companies)
      .transact(transactor)
      .void
}
