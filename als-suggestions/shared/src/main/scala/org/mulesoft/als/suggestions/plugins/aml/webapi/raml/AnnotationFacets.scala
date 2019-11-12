package org.mulesoft.als.suggestions.plugins.aml.webapi.raml

import amf.core.model.domain.Shape
import amf.core.model.domain.extensions.CustomDomainProperty
import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.plugins.aml._
import org.mulesoft.amfmanager.dialect.webapi.raml.raml10.Raml10TypesDialect

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AnnotationFacets extends AMLCompletionPlugin {
  override def id: String = "AnnotationFacets"

  override def resolve(request: AmlCompletionRequest): Future[Seq[RawSuggestion]] = {
    Future {
      request.branchStack.headOption match {
        case Some(c: CustomDomainProperty) if isWrittingFacet(request) =>
          Raml10TypesDialect.AnnotationType.propertiesRaw()
        case _ => Nil
      }
    }
  }

  private def isWrittingFacet(request: AmlCompletionRequest): Boolean =
    request.yPartBranch.isKey && (request.amfObject match {
      case s: Shape => s.name.value() != request.yPartBranch.stringValue
      case _        => false
    })
}
