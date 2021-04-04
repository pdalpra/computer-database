import java.time._
import java.time.format.DateTimeFormatter

lintUnusedKeysOnLoad in Global := false

scalaVersion := "2.13.3"
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

val circeVersion      = "0.13.0"
val doobieVersion     = "0.10.0"
val enumeratumVersion = "1.6.1"
val fs2Version        = "2.5.4"
val http4sVersion     = "0.21.16"
val pureconfigVersion = "0.14.1"
val refinedVersion    = "0.9.23"

libraryDependencies ++= Seq(
  // Cats / Cats Effect
  "org.typelevel" %% "cats-core"   % "2.5.0",
  "org.typelevel" %% "cats-effect" % "2.4.1",
  "org.typelevel" %% "kittens"     % "2.2.1",
  "org.typelevel" %% "mouse"       % "1.0.2",
  // Http4s / Scalatags
  "org.http4s"  %% "http4s-circe"        % http4sVersion,
  "org.http4s"  %% "http4s-dsl"          % http4sVersion,
  "org.http4s"  %% "http4s-blaze-server" % http4sVersion,
  "com.lihaoyi" %% "scalatags"           % "0.9.4",
  // FS2
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io"   % fs2Version,
  // Circe
  "io.circe" %% "circe-core"    % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion,
  "io.circe" %% "circe-refined" % circeVersion,
  "io.circe" %% "circe-fs2"     % circeVersion,
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
  "io.chrisdavenport" %% "log4cats-slf4j"  % "1.1.1",
  "ch.qos.logback"     % "logback-classic" % "1.2.3" % Runtime,
  // Misc
  "com.beachape" %% "enumeratum"      % enumeratumVersion,
  "com.beachape" %% "enumeratum-cats" % enumeratumVersion,
  // Testing
  "org.scalatest"     %% "scalatest"       % "3.2.7"   % "test,it",
  "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0" % "test",
  "org.scalacheck"    %% "scalacheck"      % "1.15.3"  % "test"
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
