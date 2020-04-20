import java.time._
import java.time.format.DateTimeFormatter

scalaVersion := "2.13.1"
scalafmtOnCompile := true

lazy val It = config("it") extend Test
configs(It)
inConfig(It)(Defaults.itSettings)
inConfig(It)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings)

buildInfoPackage := "com.github.pdalpra.computerdb"
buildInfoOptions += BuildInfoOption.ToJson
buildInfoKeys := Seq(
  "name"           -> name.value,
  "buildTimestamp" -> ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
  "commit"         -> git.gitHeadCommit.value
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

val circeVersion      = "0.13.0"
val doobieVersion     = "0.9.0"
val fs2Version        = "2.3.0"
val http4sVersion     = "0.21.3"
val pureconfigVersion = "0.12.3"
val refinedVersion    = "0.9.13"

libraryDependencies ++= Seq(
  // Cats / Cats Effect
  "org.typelevel" %% "cats-core"   % "2.1.1",
  "org.typelevel" %% "cats-effect" % "2.1.3",
  "org.typelevel" %% "kittens"     % "2.0.0",
  "org.typelevel" %% "mouse"       % "0.25",
  // ZIO
  "dev.zio" %% "zio"              % "1.0.0-RC18-2",
  "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC12",
  // Http4s
  "org.http4s" %% "http4s-dsl"          % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  // Scalatags
  "com.lihaoyi" %% "scalatags"        % "0.9.0",
  "org.http4s"  %% "http4s-scalatags" % http4sVersion,
  // FS2
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io"   % fs2Version,
  // Circe
  "io.circe" %% "circe-core"    % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion,
  "io.circe" %% "circe-refined" % circeVersion,
  "io.circe" %% "circe-fs2"     % "0.13.0",
  // Doobie
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-h2"   % doobieVersion,
  // Pureconfig
  "com.github.pureconfig" %% "pureconfig"             % pureconfigVersion,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % pureconfigVersion,
  // Refined
  "eu.timepit"   %% "refined"            % refinedVersion,
  "eu.timepit"   %% "refined-cats"       % refinedVersion,
  "eu.timepit"   %% "refined-pureconfig" % refinedVersion,
  "org.tpolecat" %% "doobie-refined"     % doobieVersion,
  // Logging
  "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1",
  "ch.qos.logback"    % "logback-classic" % "1.2.3" % Runtime,
  // Misc
  "com.beachape" %% "enumeratum" % "1.5.15",
  // Testing
  "org.scalatest"     %% "scalatest"       % "3.1.1"   % "test,it",
  "org.scalatestplus" %% "scalacheck-1-14" % "3.1.1.1" % "test",
  "org.scalacheck"    %% "scalacheck"      % "1.14.3"  % "test"
)

addCommandAlias(
  "ci-checks",
  List(
    "all clean scalafmtSbtCheck scalafmtCheckAll",
    "coverage",
    "test",
    "it:test",
    "coverageReport"
  ).mkString(";", ";", "")
)
