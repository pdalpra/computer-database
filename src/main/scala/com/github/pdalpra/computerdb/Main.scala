package com.github.pdalpra.computerdb

import cats.effect.ExitCode
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object Main extends CatsApp {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    new ComputerDatabase[Task].program
      .fold(_ => ExitCode.Error, _ => ExitCode.Success)
      .map(_.code)
}
