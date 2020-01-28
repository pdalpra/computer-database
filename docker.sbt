enablePlugins(BuildInfoPlugin, JavaAppPackaging, DockerPlugin)
enablePlugins(DockerSpotifyClientPlugin)

dockerBaseImage := "openjdk:11-jdk-slim"
dockerExposedPorts := Seq(8080)
