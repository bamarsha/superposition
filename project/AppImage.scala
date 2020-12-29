import java.nio.file.Files.copy
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import sbt._

import scala.reflect.io.Directory
import scala.sys.process.stringSeqToProcess

/** A module for creating application images. */
object AppImage {
  /** Creates an application image.
    *
    * @param name The name of the image.
    * @param jar The JAR from which to create the image.
    * @param mainClass The name of the main class in the JAR.
    * @param log The logger.
    * @return The created application image file.
    */
  def create(name: String, jar: File, mainClass: String, log: Logger): File = {
    val baseDir = jar.getParentFile / "app-image"
    val inputDir = baseDir / "input"
    val destDir = baseDir / "dest"
    val inputJar = inputDir / jar.getName
    if (jar.lastModified <= destDir.lastModified) {
      log.info(s"Application image is already up-to-date in: ${destDir.getAbsolutePath}")
    } else {
      Directory(destDir).deleteRecursively()
      inputDir.mkdirs()
      copy(jar.toPath, inputJar.toPath, REPLACE_EXISTING)
      jpackage(inputDir, destDir, name, inputJar, mainClass)
      log.info(s"Created application image in: ${destDir.getAbsolutePath}")
    }
    destDir
  }

  /** Runs the `jpackage` command.
    *
    * @param input The path of the input directory that contains the files to be packaged.
    * @param dest The path where generated output file is placed.
    * @param name The name of the application and/or package.
    * @param mainJar The main JAR of the application.
    * @param mainClass The qualified name of the application main class to execute.
    */
  private def jpackage(input: File, dest: File, name: String, mainJar: File, mainClass: String): Unit = {
    val args = List(
      "jpackage",
      "--type", "app-image",
      "--input", input.getAbsolutePath,
      "--dest", dest.getAbsolutePath,
      "--name", name,
      "--main-jar", mainJar.relativeTo(input).get.getPath,
      "--main-class", mainClass,
      "--java-options", "-XX:+UseZGC")
    assert(args.! == 0, "jpackage finished with non-zero exit code.")
  }
}
