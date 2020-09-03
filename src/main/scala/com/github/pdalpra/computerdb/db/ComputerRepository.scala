package com.github.pdalpra.computerdb.db

import java.time.LocalDate

import com.github.pdalpra.computerdb.model._

import cats.effect.Sync
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.refined.implicits._

trait ComputerRepository[F[_]] {
  def fetchOne(id: Computer.Id): F[Option[Computer]]
  def fetchPaged(
      page: Page.Number,
      pageSize: Page.Size,
      sort: ComputerSort,
      order: Order,
      nameFilter: Option[NonEmptyString]
  ): F[Page[Computer]]

  def update(id: Computer.Id, computer: UnsavedComputer): F[Unit]
  def insert(computer: UnsavedComputer): F[Computer]
  def deleteOne(id: Computer.Id): F[Unit]
  def loadAll(computers: List[UnsavedComputer]): F[Unit]
}

object ComputerRepository {

  def apply[F[_]: Sync](transactor: Transactor[F]): ComputerRepository[F] =
    new ComputerRepository[F] {
      override def fetchOne(id: Computer.Id): F[Option[Computer]] =
        (baseSelect ++ fr"where computer.id = $id").query[Computer].option.transact(transactor)

      def fetchPaged(
          page: Page.Number,
          pageSize: Page.Size,
          sort: ComputerSort,
          order: Order,
          nameFilter: Option[NonEmptyString]
      ): F[Page[Computer]] = {
        val offset = (page.value - 1) * pageSize.value

        val normalizedFilter = nameFilter.map(_.value.toLowerCase)
        val filterFragment   = Fragments.whereAndOpt(normalizedFilter.map(name => fr"lower(computer.name) like ${s"%$name%"}"))
        val sortFragment     = fr"order by " ++ Fragment.const(sort.column) ++ Fragment.const(order.show)
        val pageQuery        = baseSelect ++ filterFragment ++ sortFragment ++ fr"nulls last limit $pageSize offset $offset"
        val rowsCountQuery   = fr" select count(1) from computer left join company on computer.company_id = company.id" ++ filterFragment

        (for {
          list      <- pageQuery.query[Computer].to[List]
          rowsCount <- rowsCountQuery.query[Int].unique
        } yield Page(list, page, offset, rowsCount)).transact(transactor)
      }

      override def update(id: Computer.Id, computer: UnsavedComputer): F[Unit] =
        sql"""update computer
              set name = ${computer.name},
              introduced = ${computer.introduced},
              discontinued = ${computer.discontinued},
              company_id = ${computer.company}
              where id = $id
           """.update.run.transact(transactor).void

      override def insert(computer: UnsavedComputer): F[Computer] =
        (for {
          id       <- insertComputer.withUniqueGeneratedKeys[Computer.Id]("id")(computer)
          computer <- (baseSelect ++ fr"where computer.id = $id").query[Computer].unique
        } yield computer).transact(transactor)

      override def deleteOne(id: Computer.Id): F[Unit] =
        sql"delete from computer where id = $id".update.run.transact(transactor).void

      override def loadAll(computers: List[UnsavedComputer]): F[Unit] =
        (for {
          _ <- sql"delete from computer".update.run
          _ <- sql"alter sequence computer_seq restart with 1".update.run
          _ <- insertComputer.updateMany(computers)
        } yield ()).transact(transactor)

      private val baseSelect =
        fr"""select computer.id, computer.name, computer.introduced, computer.discontinued, company.id, company.name
            from computer
            left join company on computer.company_id = company.id"""

      private val insertComputer =
        Update[UnsavedComputer]("insert into computer(name, introduced, discontinued, company_id) values (?,?,?,?)")

      @SuppressWarnings(Array("org.wartremover.warts.TryPartial"))
      private implicit val computerRead: Read[Computer] =
        Read[(Computer.Id, NonEmptyString, Option[LocalDate], Option[LocalDate], Option[Company])]
          .map((Computer.apply _).tupled(_).leftMap(new IllegalArgumentException(_)).toTry.get)
    }
}
