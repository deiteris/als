package org.mulesoft.als.server.protocol.configuration

import org.mulesoft.als.server.feature.workspace.ProjectConfigurationParams
import org.mulesoft.als.server.protocol.convert.LspConvertersSharedToClient.ClientProjectConfigurationTupleConverter

import scala.scalajs.js

// $COVERAGE-OFF$ Incompatibility between scoverage and scalaJS

@js.native
trait ClientProjectConfigurationParams extends js.Object {
  def configFile: js.UndefOr[ClientProjectConfigurationTuple] = js.native
}

object ClientProjectConfigurationParams {
  def apply(internal: ProjectConfigurationParams): ClientProjectConfigurationParams = {
    js.Dynamic
      .literal(
        configFile = internal.configFile.map(_.toClient).getOrElse(null)
      )
      .asInstanceOf[ClientProjectConfigurationParams]
  }
}
// $COVERAGE-ON$
