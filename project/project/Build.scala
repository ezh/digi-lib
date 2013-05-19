import sbt._
object PluginDef extends Build {
  override def projects = Seq(root)
  lazy val root = Project("plugins", file(".")) dependsOn(osgi, scct, coveralls)
  lazy val scct = uri("git://github.com/ezh/sbt-scct.git#dbda90348a260995dea29eee97d03457742a8a67")
  lazy val coveralls = uri("git://github.com/theon/xsbt-coveralls-plugin.git#3ca29e0a9201825a0df463ca6cfdb4657bef5255")
  lazy val osgi = uri("git://github.com/digimead/sbt-osgi-manager.git#eef9aa49d9f6733272ef5c3f28318e563a34ca00")
}
