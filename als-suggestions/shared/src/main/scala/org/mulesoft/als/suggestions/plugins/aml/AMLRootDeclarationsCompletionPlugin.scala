package org.mulesoft.als.suggestions.plugins.aml

import amf.core.model.document.{DeclaresModel, Document, Module}
import amf.plugins.document.vocabularies.metamodel.domain.DocumentsModelModel
import amf.plugins.document.vocabularies.model.document.{DialectInstance, DialectInstanceLibrary}
import amf.plugins.document.vocabularies.model.domain.PublicNodeMapping
import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.plugins.aml.categories.CategoryRegistry
import org.mulesoft.amfintegration.AmfImplicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AMLRootDeclarationsCompletionPlugin(params: AmlCompletionRequest) {

  def extractText(mapping: PublicNodeMapping): (String, String) =
    if (mapping.mappedNode().isNullOrEmpty) (s"${mapping.name().value()}", "")
    else (s"${mapping.name().value()}", "\n  ")

  private def getSuggestions: Seq[(String, String)] =
    params.baseUnit match {
      case d: DeclaresModel if params.actualDialect.documents().declarationsPath().option().isDefined =>
        params.actualDialect
          .documents()
          .declarationsPath()
          .option()
          .map(v => (v, "\n  "))
          .toSeq // todo: should this be indentation from config?
      case d: DeclaresModel =>
        params.baseUnit match {
          case _: DialectInstanceLibrary | _: Module =>
            params.actualDialect
              .documents()
              .library()
              .declaredNodes()
              .map(extractText)
          case _ =>
            params.actualDialect
              .documents()
              .root()
              .declaredNodes()
              .map(extractText)
        }
      case _ => Nil
    }

  def usesSuggestion(): Option[(String, String)] =
    params.actualDialect.documents().fields.getValueAsOption(DocumentsModelModel.Library).map(_ => ("uses", "\n  "))

  def resolve(classTerm: String): Future[Seq[RawSuggestion]] =
    Future {
      (getSuggestions ++ usesSuggestion())
        .map(s => RawSuggestion.forObject(s._1, CategoryRegistry(classTerm, s._1, params.actualDialect.id)))
    }
}

object AMLRootDeclarationsCompletionPlugin extends AMLCompletionPlugin {
  override def id = "AMLRootDeclarationsCompletionPlugin"

  override def resolve(params: AmlCompletionRequest): Future[Seq[RawSuggestion]] = {
    if (params.yPartBranch.isAtRoot && params.yPartBranch.isKey && !isInFieldValue(params))
      new AMLRootDeclarationsCompletionPlugin(params)
        .resolve(params.amfObject.metaURIs.head)
    else emptySuggestion
  }
}
