name := "RGBEngine"
version := "1.0-SNAPSHOT"

autoScalaLibrary := false

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= {
  val version = "3.2.4-SNAPSHOT"
  val os = "windows"
  Seq("lwjgl", "lwjgl-glfw", "lwjgl-opengl", "lwjgl-stb") flatMap { module =>
    Seq(
      "org.lwjgl" % module % version,
      "org.lwjgl" % module % version classifier s"natives-$os")
  }
}
libraryDependencies += "org.joml" % "joml" % "1.9.19"
