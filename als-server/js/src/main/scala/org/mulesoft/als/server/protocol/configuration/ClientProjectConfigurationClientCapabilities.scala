package org.mulesoft.als.server.protocol.configuration

import org.mulesoft.als.server.feature.workspace.ProjectConfigurationClientCapabilities
import org.mulesoft.als.server.protocol.convert.LspConvertersSharedToClient.ClientProjectConfigurationParamsConverter

import scala.scalajs.js

// $COVERAGE-OFF$ Incompatibility between scoverage and scalaJS

@js.native
trait ClientProjectConfigurationClientCapabilities extends js.Object {
  def usesConfiguration: ClientProjectConfigurationParams = js.native
}

object ClientProjectConfigurationClientCapabilities {
  def apply(internal: ProjectConfigurationClientCapabilities): ClientProjectConfigurationClientCapabilities = {
    js.Dynamic
      .literal(
        usesConfiguration = internal.usesConfiguration.toClient
      )
      .asInstanceOf[ClientProjectConfigurationClientCapabilities]
  }
}
// $COVERAGE-ON$
