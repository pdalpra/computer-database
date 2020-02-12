package com.github.pdalpra.computerdb.db.sql

import com.github.pdalpra.computerdb.model.{ Company, NonEmptyString }

import eu.timepit.refined.auto._
import zio._
import zio.interop.catz._

class SqlCompanyRepositorySpec extends SqlRepositorySpec {

  "loadAll" should "be able to load a list of companies successfully" in {
    val companiesNames: List[NonEmptyString] = List("company1", "company2")

    val loadCompanies = testTransactor.use { tx =>
      val companyRepository = new SqlCompanyRepository[Task](tx)

      for {
        _ <- companyRepository.loadAll(companiesNames)
      } yield ()
    }

    noException should be thrownBy runtime.unsafeRun(loadCompanies)
  }

  "fetchAll" should "be able to fetch the list of all companies" in {
    val companiesNames: List[NonEmptyString] = List("company1", "company2")

    val fetchCompanies = testTransactor.use { tx =>
      val companyRepository = new SqlCompanyRepository[Task](tx)

      for {
        _         <- companyRepository.loadAll(companiesNames)
        companies <- companyRepository.fetchAll
      } yield companies

    }

    val companies = runtime.unsafeRun(fetchCompanies)

    companies should contain allOf (
      Company(Company.Id(1L), "company1"),
      Company(Company.Id(2L), "company2")
    )
  }
}
