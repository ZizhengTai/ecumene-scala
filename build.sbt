lazy val root = (project in file(".")).
  settings(
    name := "ecumene",
    version := "0.1",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "org.zeromq" % "jeromq" % "0.3.5",
      "org.msgpack" % "msgpack-core" % "0.7.0-M6",
      "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
    )
  )
