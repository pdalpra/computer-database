package com.github.pdalpra.computerdb.db.sql

import com.github.pdalpra.computerdb.model.{ Company, Computer, NonEmptyString, UnsavedComputer }

import eu.timepit.refined.auto._
import zio._
import zio.interop.catz._

class SqlComputerRepositorySpec extends SqlRepositorySpec {

  "loadAll" should "be able to load a list of computers successfully" in {
    val companiesNames: List[NonEmptyString] = List("company1", "company2")
    val computers = List(
      UnsavedComputer("Computer 1", None, None, Some(Company.Id(1L))),
      UnsavedComputer("Computer 2", None, None, Some(Company.Id(1L)))
    )

    val loadComputers = testTransactor.use { tx =>
      val companyRepository  = new SqlCompanyRepository[Task](tx)
      val computerRepository = new SqlComputerRepository[Task](tx, Nil)

      for {
        _ <- companyRepository.loadAll(companiesNames)
        _ <- computerRepository.loadAll(computers)
      } yield ()
    }

    noException should be thrownBy runtime.unsafeRun(loadComputers)
  }

  it should "reset computer id sequence on when reloading data" in {
    val companiesNames: List[NonEmptyString] = List("company1", "company2")
    val computers = List(
      UnsavedComputer("Computer 1", None, None, Some(Company.Id(1L))),
      UnsavedComputer("Computer 2", None, None, Some(Company.Id(1L)))
    )

    val loadComputer = testTransactor.use { tx =>
      val companyRepository  = new SqlCompanyRepository[Task](tx)
      val computerRepository = new SqlComputerRepository[Task](tx, Nil)

      for {
        _        <- companyRepository.loadAll(companiesNames)
        _        <- computerRepository.loadAll(computers)
        computer <- computerRepository.fetchOne(Computer.Id(3L))
      } yield computer
    }

    val computer = runtime.unsafeRun(loadComputer)

    computer shouldBe empty
  }
}
