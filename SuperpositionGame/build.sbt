name := "SuperpositionGame"
version := "0.2-SNAPSHOT"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.0-M32"
libraryDependencies += "com.beachape" %% "enumeratum" % "1.5.15"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
  case path =>
    val strategy = (assemblyMergeStrategy in assembly).value
    strategy(path)
}
