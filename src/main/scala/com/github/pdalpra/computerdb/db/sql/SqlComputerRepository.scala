package com.github.pdalpra.computerdb.db.sql

import com.github.pdalpra.computerdb.db._
import com.github.pdalpra.computerdb.db.sql.DoobieInstances._
import com.github.pdalpra.computerdb.model._

import cats.effect.Sync
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.refined.implicits._

object SqlComputerRepository {
  private val DefaultPageSize: Page.Size = Page.Size.unsafeFrom(10)
}
class SqlComputerRepository[F[_]: Sync](transactor: Transactor[F], readOnlyComputers: List[Computer.Id]) extends ComputerRepository[F] {

  override def fetchOne(id: Computer.Id): F[Option[Computer]] =
    (baseSelect ++ fr"where computer.id = $id").query[Computer].option.transact(transactor)

  def fetchPaged(
      page: Page.Number,
      pageSize: Option[Page.Size],
      sort: ComputerSort,
      order: Order,
      nameFilter: Option[NonEmptyString]
  ): F[Page[Computer]] = {
    val limit  = pageSize.getOrElse(SqlComputerRepository.DefaultPageSize)
    val offset = (page.value - 1) * limit.value

    val filterFragment = Fragments.whereAndOpt(nameFilter.map(name => fr"lower(computer.name) like ${s"%$name%"}"))
    val sortFragment   = fr"order by " ++ Fragment.const(sort.column) ++ Fragment.const(order.entryName)
    val pageQuery      = baseSelect ++ filterFragment ++ sortFragment ++ fr"nulls last limit $limit offset $offset"
    val rowsCountQuery = fr" select count(1) from computer left join company on computer.company_id = company.id" ++ filterFragment

    (for {
      list      <- pageQuery.query[Computer].to[List]
      rowsCount <- rowsCountQuery.query[Int].unique
    } yield Page(list, page, offset, rowsCount)).transact(transactor)
  }

  override def update(id: Computer.Id, computer: UnsavedComputer): F[Unit] = {
    val updateQuery =
      sql"""update computer
              set name = ${computer.name},
              introduced = ${computer.introduced},
              discontinued = ${computer.discontinued},
              company_id = ${computer.company}
              where id = $id
           """

    if (!readOnlyComputers.contains(id)) updateQuery.update.run.transact(transactor).void
    else ().pure[F]
  }

  override def insert(computer: UnsavedComputer): F[Computer] =
    (for {
      id       <- insertComputer.withUniqueGeneratedKeys[Computer.Id]("id")(computer)
      computer <- (baseSelect ++ fr"where id = $id").query[Computer].unique
    } yield computer).transact(transactor)

  override def deleteOne(id: Computer.Id): F[Unit] =
    if (!readOnlyComputers.contains(id)) sql"delete from computer where id = $id".update.run.transact(transactor).void
    else ().pure[F]

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

}
