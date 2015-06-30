//
// Copyright (c) 2012-2015 Alexey Aksenov ezh@ezh.msk.ru
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// DEVELOPMENT CONFIGURATION

import sbt.aspectj.nested._
import sbt.osgi.manager._

AspectJNested ++ OSGiManager // ++ sbt.scct.ScctPlugin.instrumentSettings - ScctPlugin is broken, have no time to fix

name := "digi-lib"

description := "Base library for Digi components"

licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

organization := "org.digimead"

organizationHomepage := Some(url("http://digimead.org"))

homepage := Some(url("https://github.com/ezh/digi-lib"))

version <<= (baseDirectory) { (b) => scala.io.Source.fromFile(b / "version").mkString.trim }

inConfig(OSGiConf)({
  import OSGiKey._
  Seq(
    osgiBndBundleActivator := "org.digimead.digi.lib.Activator",
    osgiBndBundleSymbolicName := "org.digimead.digi.lib",
    osgiBndBundleCopyright := "Copyright © 2011-2015 Alexey B. Aksenov/Ezh. All rights reserved.",
    osgiBndExportPackage := List("org.digimead.*", "com.escalatesoft.subcut.inject"),
    osgiBndImportPackage := List("!org.aspectj.*", "com.escalatesoft.subcut.inject", "*"),
    osgiBndBundleLicense := "http://www.apache.org/licenses/LICENSE-2.0.txt;description=The Apache Software License, Version 2.0"
  )
})

crossScalaVersions := Seq("2.11.6")

scalaVersion := "2.11.6"

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-Xcheckinit", "-feature")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

//
// AspectJ
//

AJKey.aspectjSource in AJConf <<= sourceDirectory(_ / "test" / "aspectj")

AJKey.aspectjInputs in AJConf <<= (classDirectory in Compile, classDirectory in Test) map {(a,b) => Seq(a,b)}

AJKey.aspectjFilter in AJConf := { (input, aspects) =>
  input.name match {
    case "test-classes" => aspects filter (_.toString.contains("/aspectj/org/digimead/digi/lib/aop/internal/"))
    case other => Seq.empty
  }
}

AJKey.aspectjClasspath in AJConf <<= (dependencyClasspath in Test) map { _.files }

AJKey.aspectjInputResources in AJConf <<= copyResources in Test

products in Test <<= (products in Test, AJKey.aspectjWeaveArg in AJConf, AJKey.aspectjGenericArg in AJConf) map { (_, a, b) => AspectJ.weave(a)(b) }

//
// Custom local options
//

resolvers += "digimead-maven" at "http://storage.googleapis.com/maven.repository.digimead.org/"

libraryDependencies ++= Seq(
    "com.escalatesoft.subcut" %% "subcut" % "2.1",
    // https://issues.scala-lang.org/browse/SI-7751
    // .../guava-15.0.jar(com/google/common/cache/CacheBuilder.class)' is broken
    // [error] (class java.lang.RuntimeException/bad constant pool index: 0 at pos: 15214)
    "com.google.code.findbugs" % "jsr305" % "3.0.0",
    "com.google.guava" % "guava" % "18.0",
    "com.typesafe.akka" %% "akka-actor" % "2.3.11",
    "org.apache.felix" % "org.apache.felix.log" % "1.0.1" % "test",
    "org.aspectj" % "aspectjrt" % "1.8.5",
    "org.digimead" %% "digi-lib-test" % "0.3.2.0" % "test",
    "org.osgi" % "org.osgi.core" % "5.0.0",
    "org.osgi" % "org.osgi.compendium" % "4.3.1",
    "org.slf4j" % "slf4j-api" % "1.7.12"
  )

// scala-compiler is required by SubCut, so add explicitly the current version
libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

//
// Testing
//

parallelExecution in Test := false

testGrouping in Test <<= (definedTests in Test) map { tests =>
  tests map { test =>
    new Tests.Group(
      name = test.name,
      tests = Seq(test),
      runPolicy = Tests.SubProcess(ForkOptions(runJVMOptions = Seq.empty[String])))
  }
}

//logLevel := Level.Debug
