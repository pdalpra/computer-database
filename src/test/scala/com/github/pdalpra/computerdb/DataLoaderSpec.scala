package com.github.pdalpra.computerdb

import zio._
import zio.interop.catz._

class DataLoaderSpec extends ComputerDatabaseSpec {

  "DataLoaderSpec" should "be able to load reference data without errors" in {
    val dataLoader = DataLoader[Task](blocker)

    noException should be thrownBy runtime.unsafeRun(dataLoader.loadInitialData)
  }
}
