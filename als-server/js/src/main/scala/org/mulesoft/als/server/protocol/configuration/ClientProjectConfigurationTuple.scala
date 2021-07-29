package org.mulesoft.als.server.protocol.configuration

import org.mulesoft.als.server.feature.workspace.ProjectConfigurationTuple

import scala.scalajs.js

// $COVERAGE-OFF$ Incompatibility between scoverage and scalaJS

@js.native
trait ClientProjectConfigurationTuple extends js.Object {
  def fileName: String                   = js.native
  def definitionName: js.UndefOr[String] = js.native
}

object ClientProjectConfigurationTuple {
  def apply(internal: ProjectConfigurationTuple): ClientProjectConfigurationTuple = {
    js.Dynamic
      .literal(
        fileName = internal.fileName,
        definitionName = internal.definitionName.orNull
      )
      .asInstanceOf[ClientProjectConfigurationTuple]
  }
}

// $COVERAGE-ON$
