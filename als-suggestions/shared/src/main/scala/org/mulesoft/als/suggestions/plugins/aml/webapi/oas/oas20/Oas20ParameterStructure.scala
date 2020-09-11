package org.mulesoft.als.suggestions.plugins.aml.webapi.oas.oas20

import amf.core.annotations.AutoGeneratedName
import amf.core.model.domain.Shape
import amf.plugins.domain.shapes.models.{AnyShape, ScalarShape}
import amf.plugins.domain.webapi.metamodel.{EndPointModel, ParameterModel, RequestModel}
import amf.plugins.domain.webapi.models.{EndPoint, Parameter, Request}
import org.mulesoft.als.common.YPartBranch
import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.plugins.NonPatchHacks
import org.mulesoft.als.suggestions.plugins.aml._
import org.mulesoft.als.suggestions.plugins.aml.categories.CategoryRegistry
import org.mulesoft.amfintegration.dialect.dialects.oas.OAS20Dialect
import org.mulesoft.amfintegration.dialect.dialects.oas.nodes.Oas20ParamObject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Oas20ParameterStructure extends AMLCompletionPlugin with NonPatchHacks {
  override def id: String = "ParameterStructure"

  override def resolve(request: AmlCompletionRequest): Future[Seq[RawSuggestion]] = {
    Future {
      if (isWritingFacet(request.yPartBranch)) {
        request.amfObject match {
          case p: Parameter if p.binding.option().contains("header") && comesFromHeader(request.yPartBranch) =>
            if (p.name.value() != request.yPartBranch.stringValue)
              Oas20TypeFacetsCompletionPlugin.resolveShape(Option(p.schema).getOrElse(AnyShape()), Nil, OAS20Dialect())
            else Nil
          case p: Parameter if request.fieldEntry.isEmpty && request.yPartBranch.stringValue != p.name.value() =>
            parameterSuggestions(request, p)
          case _: EndPoint if request.fieldEntry.exists(_.field == EndPointModel.Parameters) =>
            suggestions(withName = false, None)
          case _: Request if request.fieldEntry.exists(_.field == RequestModel.QueryParameters) =>
            suggestions(withName = false, None)
          case _ => Nil
        }
      } else Nil
    }
  }

  private def parameterSuggestions(request: AmlCompletionRequest, p: Parameter) =
    suggestions(isNamePresent(p), Option(p.schema))

  private def suggestions(withName: Boolean, schema: Option[Shape]) = {
    val common = Oas20TypeFacetsCompletionPlugin.resolveShape(schema.getOrElse(ScalarShape()), Nil, OAS20Dialect())

    val particular =
      if (withName) Seq(onlyBinding) else Seq(onlyBinding, nameSuggestion)
    particular ++ common
  }

  private def isWritingFacet(yPart: YPartBranch) =
    notValue(yPart) || (yPart.isJson && yPart.isInArray && yPart.stringValue == "x")

  private def isNamePresent(p: Parameter) =
    p.name.option().isDefined && !p.name.annotations().contains(classOf[AutoGeneratedName])
  private def comesFromHeader(yPart: YPartBranch) = yPart.keys.contains("headers")

  private def onlyBinding =
    Oas20ParamObject.paramBinding.toRaw(CategoryRegistry(ParameterModel.`type`.head.iri(), "in", OAS20Dialect().id))

  private def nameSuggestion: RawSuggestion =
    Oas20ParamObject.paramName.toRaw(
      CategoryRegistry(ParameterModel.`type`.head.iri(), Oas20ParamObject.paramName.name().value(), OAS20Dialect().id))
}
