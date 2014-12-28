name := "monitor"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies ++= Seq(
  "org.webjars" % "webjars-play_2.11" % "2.3.0",
  "org.webjars" % "html5shiv" % "3.7.2",
  "org.webjars" % "respond" % "1.4.2",
  "org.webjars" % "prettify" % "4-Mar-2013",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.apache.commons" % "commons-email" % "1.3.2",
  "commons-codec" % "commons-codec" % "1.9",
  "mysql" % "mysql-connector-java" % "5.1.31"
)

libraryDependencies += "com.twitter" % "util-core_2.10" % "6.22.1"

libraryDependencies += "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.3.5" withSources()

libraryDependencies += "log4j" % "log4j" % "1.2.16"
