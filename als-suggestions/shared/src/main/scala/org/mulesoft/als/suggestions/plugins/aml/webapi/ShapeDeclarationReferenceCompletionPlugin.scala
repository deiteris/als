package org.mulesoft.als.suggestions.plugins.aml.webapi

import amf.core.annotations.SynthesizedField
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.plugins.document.vocabularies.model.domain.PropertyMapping
import amf.plugins.domain.shapes.models.UnresolvedShape
import amf.plugins.domain.webapi.models.{Parameter, Payload}
import org.mulesoft.als.common.ElementNameExtractor._
import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.plugins.aml.{AMLRamlStyleDeclarationsReferences, BooleanSuggestions}
import org.yaml.model.YMapEntry
import org.mulesoft.amfmanager.AmfImplicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ShapeDeclarationReferenceCompletionPlugin extends AMLCompletionPlugin with BooleanSuggestions {

  override def resolve(params: AmlCompletionRequest): Future[Seq[RawSuggestion]] = {
    Future {
      params.amfObject match {
        case s: Shape if params.yPartBranch.isValue =>
          val iri =
            if (s.annotations.contains(classOf[SynthesizedField]) || params.yPartBranch.isEmptyNode)
              ShapeModel.`type`.head.iri()
            else s.metaURIs.head
          val declaredSuggestions = new AMLRamlStyleDeclarationsReferences(Seq(iri),
                                                                           params.prefix,
                                                                           params.declarationProvider,
                                                                           s.name.option()).resolve()

          val name = params.amfObject.elementIdentifier()
          params.yPartBranch.parent
            .collectFirst({ case e: YMapEntry => e })
            .flatMap(_.key.asScalar.map(_.text)) match {
            case Some("type") => declaredSuggestions
            // i need to force generic shape model search for default amf parsed types

            case Some(text)
                if name.contains(text) || params.amfObject
                  .isInstanceOf[UnresolvedShape] || text == "body" ||
                  (params.branchStack.headOption.exists(h =>
                    (h.isInstanceOf[Parameter] || h.isInstanceOf[Payload]) && !isBoolean(h, text)) && name
                    .contains("schema")) =>
              declaredSuggestions ++ typeProperty
                .enum()
                .map(v => v.value().toString)
                .map(RawSuggestion.apply(_, isAKey = false))
            case Some(text) if params.branchStack.headOption.exists(h => isBoolean(h, text)) =>
              booleanSuggestions
            case _ => Nil
          }
        case _ => Nil
      }
    }
  }

  def typeProperty: PropertyMapping
}
