name := "Superposition"
version := "0.3-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.3"
Compile / scalacOptions ++= List(
  "-Xsource:3",
  "-Ymacro-annotations",
  "-opt:l:method",
  // TODO:
  //  "-opt:l:inline",
  //  "-opt-inline-from:**",
  "-feature",
  "-deprecation")
Compile / scalaSource := baseDirectory.value / "src"
Compile / resourceDirectory := baseDirectory.value / "resources"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "com.badlogicgames.ashley" % "ashley" % "1.7.3"
libraryDependencies ++= {
  val version = "1.9.11"
  List(
    "com.badlogicgames.gdx" % "gdx" % version,
    "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % version,
    "com.badlogicgames.gdx" % "gdx-platform" % version classifier "natives-desktop")
}
libraryDependencies += "com.beachape" %% "enumeratum" % "1.6.1"
libraryDependencies += "io.estatico" %% "newtype" % "0.4.4"
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
libraryDependencies += "org.typelevel" %% "cats-core" % "2.2.0"
libraryDependencies += "org.typelevel" %% "spire" % "0.17.0"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.first
  case path => (assemblyMergeStrategy in assembly).value(path)
}

lazy val appImage = taskKey[File]("Creates an application image.")
appImage := AppImage.create(name.value, assembly.value, (assembly / mainClass).value.get, streams.value.log)
