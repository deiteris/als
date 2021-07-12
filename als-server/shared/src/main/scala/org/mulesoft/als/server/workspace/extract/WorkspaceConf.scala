package org.mulesoft.als.server.workspace.extract

case class WorkspaceConf(rootFolder: String,
                         mainFile: String,
                         cachables: Set[String],
                         configReader: Option[ConfigReader],
                         validationProfiles: Set[String] = Set("file:///profile.amf")) {

  def shouldCache(iri: String): Boolean = cachables.contains(iri)
}
