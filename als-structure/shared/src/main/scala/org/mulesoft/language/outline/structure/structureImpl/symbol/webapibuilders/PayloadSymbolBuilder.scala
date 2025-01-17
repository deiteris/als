package org.mulesoft.language.outline.structure.structureImpl.symbol.webapibuilders

import amf.core.annotations.LexicalInformation
import amf.core.metamodel.Field
import amf.core.model.domain.AmfElement
import amf.plugins.document.webapi.annotations.DefaultPayload
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload
import org.mulesoft.als.common.dtoTypes.PositionRange
import org.mulesoft.amfintegration.ParserRangeImplicits._
import org.mulesoft.language.outline.structure.structureImpl.symbol.builders.{
  AmfObjectSimpleBuilderCompanion,
  StructuredSymbolBuilder,
  SymbolBuilder
}
import amf.core.parser.Range
import org.mulesoft.language.outline.structure.structureImpl.{DocumentSymbol, StructureContext}
class PayloadSymbolBuilder(override val element: Payload)(implicit val ctx: StructureContext)
    extends StructuredSymbolBuilder[Payload] {
  override def ignoreFields: List[Field] = super.ignoreFields :+ PayloadModel.Schema

  override protected val children: List[DocumentSymbol] =
    super.children ++
      Option(element.schema)
        .flatMap(s => ctx.factory.builderFor(s))
        .map(bs => bs.build().flatMap(_.children))
        .getOrElse(Nil)

  override protected val optionName: Option[String] =
    element.mediaType.option().orElse(element.name.option()).orElse(Some("payload"))

  override def build(): Seq[DocumentSymbol] = {
    if (element.annotations.contains(classOf[DefaultPayload])) children
    else super.build()
  }
}

object PayloadSymbolBuilderCompanion extends AmfObjectSimpleBuilderCompanion[Payload] {
  override def getType: Class[_ <: AmfElement] = classOf[Payload]

  override val supportedIri: String = PayloadModel.`type`.head.iri()

  override def construct(element: Payload)(implicit ctx: StructureContext): Option[SymbolBuilder[Payload]] =
    Some(new PayloadSymbolBuilder(element))
}
