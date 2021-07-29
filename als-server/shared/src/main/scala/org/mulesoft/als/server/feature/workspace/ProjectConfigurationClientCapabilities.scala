package org.mulesoft.als.server.feature.workspace

/**
  *
  * @param usesConfiguration if defined, project will be supported. In case of  `None`, the configuration
  *                          will be managed through requests, in case of "Some(fileName)" the configuration will
  *                          be managed through the `fileName` file, relative to the root.
  */
case class ProjectConfigurationClientCapabilities(usesConfiguration: ProjectConfigurationParams)
