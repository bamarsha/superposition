import java.nio.file.Files.copy
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import scala.reflect.io.Directory
import scala.sys.process.stringSeqToProcess

name := "Superposition"
version := "0.3-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.3"
Compile / scalacOptions ++= Seq(
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
  Seq(
    "com.badlogicgames.gdx" % "gdx" % version,
    "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % version,
    "com.badlogicgames.gdx" % "gdx-platform" % version classifier "natives-desktop")
}
libraryDependencies += "com.beachape" %% "enumeratum" % "1.6.1"
libraryDependencies += "io.estatico" %% "newtype" % "0.4.4"
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
libraryDependencies += "org.typelevel" %% "cats-core" % "2.1.1"
libraryDependencies += "org.typelevel" %% "spire" % "0.17.0-RC1"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.first
  case path => (assemblyMergeStrategy in assembly).value(path)
}

val appImage = taskKey[File]("Creates an application image.")
appImage := {
  val log = streams.value.log
  val originalJar = assembly.value
  val jarMainClass = (assembly / mainClass).value
  val baseDir = originalJar.getParentFile / "app-image"
  val inputDir = baseDir / "input"
  val destDir = baseDir / "dest"
  val inputJar = inputDir / originalJar.getName

  if (originalJar.lastModified <= destDir.lastModified) {
    log.info(s"Application image is already up-to-date in: ${destDir.getAbsolutePath}")
  } else {
    // jpackage needs this directory to be empty.
    Directory(destDir).deleteRecursively()

    inputDir.mkdirs()
    copy(originalJar.toPath, inputJar.toPath, REPLACE_EXISTING)
    assert(
      Seq("jpackage",
          "--type", "app-image",
          "--input", inputDir.getAbsolutePath,
          "--dest", destDir.getAbsolutePath,
          "--name", name.value,
          "--main-jar", inputJar.getName,
          "--main-class", jarMainClass.get,
          "--java-options", "-XX:+UseParallelGC")
        .! == 0,
      "Running jpackage failed.")
    log.info(s"Created application image in: ${destDir.getAbsolutePath}")
  }
  destDir
}
