package com.github.pdalpra.computerdb

import scala.concurrent.ExecutionContext

import cats.effect.Blocker
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import zio.DefaultRuntime

trait ComputerDatabaseSpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {
  protected implicit val runtime: DefaultRuntime = new DefaultRuntime {}
  protected val blocker: Blocker                 = Blocker.liftExecutionContext(ExecutionContext.global)
}
