package com.github.pdalpra.computerdb.db

import com.github.pdalpra.computerdb.model.Page.Size
import com.github.pdalpra.computerdb.model.{ Company, Computer, NonEmptyString, Page, UnsavedComputer }

import zio._

object testkit {

  def companyRepositoryWith(
      fetchAllF: Task[List[Company]] = Task.succeed(Nil),
      loadF: List[NonEmptyString] => Task[Unit] = _ => Task.unit
  ): CompanyRepository[Task] =
    new CompanyRepository[Task] {
      override def fetchAll                                 = fetchAllF
      override def loadAll(companies: List[NonEmptyString]) = loadF(companies)
    }

  def computerRepositoryWith(
      fetchOneF: Computer.Id => Task[Option[Computer]] = _ => Task.none,
      updateF: (Computer.Id, UnsavedComputer) => Task[Unit] = (_, _) => Task.unit,
      insertF: UnsavedComputer => Task[Computer] = _ => Task.never,
      deleteF: Computer.Id => Task[Unit] = _ => Task.unit,
      loadF: List[UnsavedComputer] => Task[Unit] = _ => Task.unit
  ): ComputerRepository[Task] =
    new ComputerRepository[Task] {
      override def fetchOne(id: Computer.Id) = fetchOneF(id)

      override def fetchPaged(
          page: Page.Number,
          pageSize: Option[Size],
          sort: ComputerSort,
          order: Order,
          nameFilter: Option[NonEmptyString]
      ) = ???

      override def update(id: Computer.Id, computer: UnsavedComputer) = updateF(id, computer)

      override def insert(computer: UnsavedComputer) = insertF(computer)

      override def deleteOne(id: Computer.Id) = deleteF(id)

      override def loadAll(computers: List[UnsavedComputer]) = loadF(computers)
    }
}
