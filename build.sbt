import java.time._
import java.time.format.DateTimeFormatter

lintUnusedKeysOnLoad in Global := false

scalaVersion := "2.13.8"
scalafmtOnCompile := true

lazy val It = config("it") extend Test
configs(It)
inConfig(It)(Defaults.itSettings)
inConfig(It)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings)

buildInfoPackage := "com.github.pdalpra.computerdb"
buildInfoObject := "ComputerDatabaseBuildInfo"
buildInfoOptions += BuildInfoOption.ToJson
buildInfoKeys := Seq(
  "name"           -> name.value,
  "buildTimestamp" -> ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
  "commit"         -> git.gitHeadCommit.value
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

val circeVersion      = "0.14.2"
val doobieVersion     = "1.0.0-RC2"
val enumeratumVersion = "1.7.0"
val fs2Version        = "3.2.10"
val http4sVersion     = "1.0.0-M24"
val pureconfigVersion = "0.17.1"
val refinedVersion    = "0.9.29"

libraryDependencies ++= Seq(
  // Cats / Cats Effect
  "org.typelevel" %% "cats-core"   % "2.7.0",
  "org.typelevel" %% "cats-effect" % "3.3.14",
  "org.typelevel" %% "kittens"     % "2.3.2",
  "org.typelevel" %% "mouse"       % "1.0.11",
  // Http4s / Scalatags
  "org.http4s"  %% "http4s-circe"        % http4sVersion,
  "org.http4s"  %% "http4s-dsl"          % http4sVersion,
  "org.http4s"  %% "http4s-blaze-server" % http4sVersion,
  "com.lihaoyi" %% "scalatags"           % "0.11.1",
  // FS2
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io"   % fs2Version,
  // Circe
  "io.circe" %% "circe-core"    % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion,
  "io.circe" %% "circe-refined" % circeVersion,
  "io.circe" %% "circe-fs2"     % "0.14.0",
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
  "org.typelevel" %% "log4cats-slf4j"  % "2.3.1",
  "ch.qos.logback" % "logback-classic" % "1.2.11" % Runtime,
  // Misc
  "com.beachape" %% "enumeratum"      % enumeratumVersion,
  "com.beachape" %% "enumeratum-cats" % enumeratumVersion,
  // Testing
  "org.scalatest"     %% "scalatest"       % "3.2.12"  % "test,it",
  "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0" % "test",
  "org.scalacheck"    %% "scalacheck"      % "1.16.0"  % "test"
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
