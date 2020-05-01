ThisBuild / scalaVersion := "2.13.1"

//lazy val engine = project in file("RGBEngine")
lazy val game = (project in file("SuperpositionGame"))//.dependsOn(engine)
