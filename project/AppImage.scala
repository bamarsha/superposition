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
      // jpackage needs this directory to be empty.
      Directory(destDir).deleteRecursively()

      inputDir.mkdirs()
      copy(jar.toPath, inputJar.toPath, REPLACE_EXISTING)
      assert(
        List(
          "jpackage",
          "--type", "app-image",
          "--input", inputDir.getAbsolutePath,
          "--dest", destDir.getAbsolutePath,
          "--name", name,
          "--main-jar", inputJar.getName,
          "--main-class", mainClass,
          "--java-options", "-XX:+UseParallelGC")
          .! == 0,
        "Running jpackage failed.")
      log.info(s"Created application image in: ${destDir.getAbsolutePath}")
    }
    destDir
  }
}
