addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"        % "0.1.22")
addSbtPlugin("org.scalameta"             % "sbt-scalafmt"        % "2.5.0")
addSbtPlugin("com.github.sbt"            % "sbt-native-packager" % "1.9.9")
addSbtPlugin("org.scoverage"             % "sbt-scoverage"       % "1.9.3")
addSbtPlugin("com.eed3si9n"              % "sbt-buildinfo"       % "0.11.0")
addSbtPlugin("com.github.sbt"            % "sbt-git"             % "2.0.0")
addSbtPlugin("org.wartremover"           % "sbt-wartremover"     % "3.0.4")
addSbtPlugin("io.spray"                  % "sbt-revolver"        % "0.9.1")

libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"
