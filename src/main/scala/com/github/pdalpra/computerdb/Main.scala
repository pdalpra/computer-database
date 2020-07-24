package com.github.pdalpra.computerdb

import cats.effect.{ ExitCode, IO, IOApp }

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new ComputerDatabase[IO].program.as(ExitCode.Success)
}
