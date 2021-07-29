package org.mulesoft.als.server.modules.workspace

import org.mulesoft.als.configuration.ProjectConfiguration
import org.mulesoft.als.server.feature.workspace._
import org.mulesoft.als.server.modules.configuration.ConfigurationManager
import org.mulesoft.lsp.InitializableModule

import scala.concurrent.Future

class ProjectConfigurationManager(configuration: ConfigurationManager)
    extends InitializableModule[ProjectConfigurationClientCapabilities, ProjectConfigurationServerOptions] {

  override val `type`: ProjectConfigurationConfigType.type = ProjectConfigurationConfigType

  override def initialize(): Future[Unit] = Future.unit

  override def applyConfig(config: Option[ProjectConfigurationClientCapabilities]): ProjectConfigurationServerOptions = {
    config.flatMap(_.usesConfiguration.configFile).foreach { c =>
      configuration.setConfigurationType(Some(ProjectConfiguration(c.fileName, c.definitionName)))
    }

    ProjectConfigurationServerOptions(true) // todo: return true only if the current file is supported
  }
}
