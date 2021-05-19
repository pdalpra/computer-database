addSbtPlugin("org.scalameta"    % "sbt-scalafmt"        % "2.4.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"       % "1.8.1")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"       % "0.10.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git"             % "1.0.0")
addSbtPlugin("org.wartremover"  % "sbt-wartremover"     % "2.4.13")
addSbtPlugin("io.spray"         % "sbt-revolver"        % "0.9.1")

libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"
