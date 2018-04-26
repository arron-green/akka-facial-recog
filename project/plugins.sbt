logLevel := Level.Warn
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.4")
classpathTypes += "maven-plugin"
libraryDependencies += "org.bytedeco" % "javacpp" % "1.3"