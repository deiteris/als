package org.mulesoft.als.suggestions.plugins.aml.webapi.oas.oas30

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.AmfScalar
import amf.core.parser.Value
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.{Parameter, Server}
import org.mulesoft.als.common.YPartBranch
import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.plugins.NonPatchHacks
import org.mulesoft.als.suggestions.plugins.aml._
import org.mulesoft.amfintegration.dialect.dialects.oas.OAS30Dialect
import org.mulesoft.amfintegration.dialect.dialects.oas.nodes.{Oas30AMLHeaderObject, Oas30ParamObject}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Oas30ParameterStructure extends AMLCompletionPlugin with NonPatchHacks {
  override def id: String = "ParameterStructure"

  override def resolve(request: AmlCompletionRequest): Future[Seq[RawSuggestion]] = {
    Future {
      request.amfObject match {
        case p: Parameter
            if isWritingFacet(p, request.yPartBranch) && !request.branchStack.exists(_.isInstanceOf[Server]) =>
          plainParam(p)
        case _ => Nil
      }
    }
  }

  def plainParam(p: Parameter): Seq[RawSuggestion] =
    if (synthesizedHeader(p)) headerProps else paramProps

  def synthesizedHeader(p: Parameter): Boolean = {
    p.fields
      .getValueAsOption(ParameterModel.Binding)
      .exists({
        case Value(AmfScalar("header", _), ann) => ann.contains(classOf[SynthesizedField])
        case _                                  => false
      })
  }

  private lazy val paramProps = Oas30ParamObject.Obj.propertiesRaw(d = OAS30Dialect())

  private lazy val headerProps = Oas30AMLHeaderObject.Obj.propertiesRaw(d = OAS30Dialect())

  private def isWritingFacet(p: Parameter, yPartBranch: YPartBranch) =
    (p.name.option().isEmpty || p.name.value() != yPartBranch.stringValue) && notValue(yPartBranch)
}
