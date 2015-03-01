name := "back"

version := "1.0-SNAPSHOT"

resolvers += "jgit-repository" at "http://download.eclipse.org/jgit/maven"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.jcraft" % "jsch.agentproxy" % "0.0.9",
  "com.typesafe.play" %% "play-slick" % "0.6.0.1",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "com.github.nscala-time" %% "nscala-time" % "1.0.0",
  "org.eclipse.jgit" % "org.eclipse.jgit" % "2.0.0.201206130900-r",
  "org.gitective" % "gitective-core" % "0.9.9",
  "org.apache.xmlrpc" % "xmlrpc-client" % "3.1.3"
)

play.Project.playScalaSettings

org.scalastyle.sbt.ScalastylePlugin.Settings

// Create a default Scala style task to run with tests
lazy val testScalaStyle = taskKey[Unit]("testScalaStyle")

testScalaStyle := {
      org.scalastyle.sbt.PluginKeys.scalastyle.toTask("").value
}

(test in Test) <<= (test in Test) dependsOn testScalaStyle

