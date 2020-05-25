package com.github.pdalpra.computerdb

import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object Main extends CatsApp {

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    new ComputerDatabase[Task].program.exitCode
}
