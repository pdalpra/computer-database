package com.github.pdalpra.computerdb.db

import com.github.pdalpra.computerdb.model.{ Company, NonEmptyString }

trait CompanyRepository[F[_]] {
  def fetchAll: F[List[Company]]

  def loadAll(companies: List[NonEmptyString]): F[Unit]
}
