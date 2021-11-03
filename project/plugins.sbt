addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"        % "0.1.20")
addSbtPlugin("org.scalameta"             % "sbt-scalafmt"        % "2.4.3")
addSbtPlugin("com.github.sbt"            % "sbt-native-packager" % "1.9.0")
addSbtPlugin("org.scoverage"             % "sbt-scoverage"       % "1.9.2")
addSbtPlugin("com.eed3si9n"              % "sbt-buildinfo"       % "0.10.0")
addSbtPlugin("com.typesafe.sbt"          % "sbt-git"             % "1.0.2")
addSbtPlugin("org.wartremover"           % "sbt-wartremover"     % "2.4.16")
addSbtPlugin("io.spray"                  % "sbt-revolver"        % "0.9.1")

libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"
