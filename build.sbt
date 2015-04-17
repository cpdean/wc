name := "conrad playing with akka" 

version :="0.0.1"

val akkaVersion = "2.3.7"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-agent" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "junit" % "junit" % "4.8.1" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)

mainClass in Compile := Some("playing.Main")
