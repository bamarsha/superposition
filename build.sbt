name := "Superposition"
version := "0.2-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.2"
Compile / scalacOptions ++= Seq(
  "-Xsource:3", "-Ymacro-annotations", "-deprecation", "-opt:l:method", "-opt:l:inline", "-opt-inline-from:**")
Compile / scalaSource := baseDirectory.value / "src"
Compile / resourceDirectory := baseDirectory.value / "resources"

libraryDependencies += "com.badlogicgames.ashley" % "ashley" % "1.7.3"
libraryDependencies ++= {
  val version = "1.9.11-SNAPSHOT"
  Seq(
    "com.badlogicgames.gdx" % "gdx" % version,
    "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % version,
    "com.badlogicgames.gdx" % "gdx-platform" % version classifier "natives-desktop")
}
libraryDependencies += "com.beachape" %% "enumeratum" % "1.6.0"
libraryDependencies += "io.estatico" %% "newtype" % "0.4.4"
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"
libraryDependencies += "org.typelevel" %% "spire" % "0.17.0-M1"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.first
  case path => (assemblyMergeStrategy in assembly).value(path)
}

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
