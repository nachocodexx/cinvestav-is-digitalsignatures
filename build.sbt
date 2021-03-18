lazy val fs2Version = "3.0.0-M9"
lazy val Fs2 = Seq(
  "co.fs2" %% "fs2-io",
  "co.fs2" %% "fs2-core"
).map(_%fs2Version)
lazy val ScalaTest ="org.scalatest" %% "scalatest" % "3.2.5" % Test
lazy val app = (project in file(".")).settings(
  name := "cinvestav-is-dsdct",
  version := "0.1",
  scalaVersion := "2.13.5",
  libraryDependencies ++= Seq(ScalaTest) ++ Fs2,
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")


)
