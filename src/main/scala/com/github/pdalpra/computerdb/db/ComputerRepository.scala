package com.github.pdalpra.computerdb.db

import com.github.pdalpra.computerdb.model._

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
