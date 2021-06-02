package com.github.pdalpra.computerdb

import cats.effect.{ IO, IOApp }

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    new ComputerDatabase[IO].program
}
