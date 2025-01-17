package org.mulesoft.als.server.modules.workspace

import amf.core.errorhandling.ErrorCollector
import amf.core.model.document.BaseUnit
import amf.plugins.document.vocabularies.model.document.Dialect
import org.mulesoft.als.common.dtoTypes.ReferenceStack
import org.mulesoft.als.configuration.{UnitWithWorkspaceConfiguration, WorkspaceConfiguration}
import org.mulesoft.amfintegration.AmfImplicits._
import org.mulesoft.amfintegration.ErrorsCollected

import scala.concurrent.Future

case class ParsedUnit(bu: BaseUnit,
                      inTree: Boolean,
                      definedBy: Dialect,
                      eh: ErrorCollector,
                      override val workspaceConfiguration: Option[WorkspaceConfiguration])
    extends UnitWithWorkspaceConfiguration {

  def toCU(next: Option[Future[CompilableUnit]],
           mf: Option[String],
           stack: Seq[ReferenceStack],
           isDirty: Boolean = false,
           workspaceConfiguration: Option[WorkspaceConfiguration]): CompilableUnit =
    CompilableUnit(bu.identifier,
                   bu,
                   if (inTree) mf else None,
                   stack,
                   isDirty,
                   next,
                   definedBy,
                   ErrorsCollected(eh.getErrors),
                   workspaceConfiguration)
}
