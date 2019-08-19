package org.mulesoft.als.suggestions.plugins.aml.webapi.raml

import amf.core.metamodel.domain.ShapeModel
import amf.core.model.DataType
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfObject, Shape}
import amf.plugins.document.vocabularies.model.domain.NodeMapping
import amf.plugins.domain.shapes.models.ScalarShape
import org.mulesoft.als.common.YPartBranch
import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.plugins.aml._

import scala.concurrent.Future

object RamlTypeFacetsCompletionPlugin extends AMLCompletionPlugin {
  override def id: String = "RamlTypeFacetsCompletionPlugin"

  override def resolve(params: AmlCompletionRequest): Future[Seq[RawSuggestion]] = {
    Future.successful(params.amfObject match {
      case shape: Shape if isWrittingFacet(params.yPartBranch, shape) =>
        resolveShape(shape, params.branchStack, params.indentation)
      case _ => Nil
    })
  }

  private def isWrittingFacet(yPartBranch: YPartBranch, shape: Shape): Boolean =
    yPartBranch.isKey && shape.name.value() != yPartBranch.stringValue

  def resolveShape(shape: Shape, branchStack: Seq[AmfObject], indentation: String): Seq[RawSuggestion] = {

    val node = shape match {
      case scalar: ScalarShape =>
        scalar.dataType.option() match {
          case Some(
              DataType.Integer | DataType.Decimal | DataType.Double | DataType.Float | DataType.Long |
              DataType.Number) =>
            Some(Raml10TypesDialect.NumberShapeNode)
          case _ => Some(Raml10TypesDialect.StringShapeNode)
        }
      case _ =>
        Raml10TypesDialect.dialect.declares
          .collect({ case n: NodeMapping => n })
          .find(_.nodetypeMapping.option().contains(shape.meta.`type`.head.iri()))
    }

    val classSuggestions = node.map(n => n.propertiesRaw(indentation)).getOrElse(Nil)

    // corner case, property shape should suggest facets of the range PLUS required
    val finalSuggestions: Iterable[RawSuggestion] with (RawSuggestion with Int => Any) = branchStack.headOption match {
      case Some(_: PropertyShape) =>
        (Raml10TypesDialect.PropertyShapeNode.propertiesRaw(indentation) ++ classSuggestions).toSet
      case _ => classSuggestions
    }

    finalSuggestions.toSeq
  }

}