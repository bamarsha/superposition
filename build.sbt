name := "Superposition"
version := "0.2-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.2"
Compile / scalacOptions += "-Xsource:2.14"
Compile / scalaSource := baseDirectory.value / "src"
Compile / resourceDirectory := baseDirectory.value / "resources"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.0"
libraryDependencies += "com.beachape" %% "enumeratum" % "1.6.0"
libraryDependencies ++= {
  val version = "1.9.10"
  Seq(
    "com.badlogicgames.gdx" % "gdx" % version,
    "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % version,
    "com.badlogicgames.gdx" % "gdx-platform" % version classifier "natives-desktop")
}
libraryDependencies += "com.badlogicgames.ashley" % "ashley" % "1.7.3"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
  case path =>
    val strategy = (assemblyMergeStrategy in assembly).value
    strategy(path)
}
