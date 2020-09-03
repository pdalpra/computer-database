package com.github.pdalpra.computerdb.db

import com.github.pdalpra.computerdb.model._

import cats.effect.Sync
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.refined.implicits._

trait CompanyRepository[F[_]] {
  def fetchAll: F[List[Company]]

  def loadAll(companies: List[NonEmptyString]): F[Unit]
}

object CompanyRepository {

  def apply[F[_]: Sync](transactor: Transactor[F]): CompanyRepository[F] =
    new CompanyRepository[F] {
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
}
