package org.mulesoft.language.outline.structure.structureImpl.symbol.webapibuilders

import amf.core.metamodel.Field
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfElement
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter
import org.mulesoft.language.outline.structure.structureImpl.symbol.builders.{
  AmfObjectSimpleBuilderCompanion,
  SymbolBuilder
}
import org.mulesoft.language.outline.structure.structureImpl.symbol.corebuilders.NamedElementSymbolBuilderTrait
import org.mulesoft.language.outline.structure.structureImpl.{DocumentSymbol, StructureContext}

class ParameterSymbolBuilder(override val element: Parameter)(implicit val ctx: StructureContext)
    extends NamedElementSymbolBuilderTrait[Parameter] {
  override def ignoreFields: List[Field] =
    super.ignoreFields :+ ParameterModel.Schema :+ DomainElementModel.CustomDomainProperties

  override protected def children: List[DocumentSymbol] =
    super.children ++ Option(element.schema)
      .flatMap(ctx.factory.builderFor)
      .map(bs => bs.build().flatMap(_.children))
      .getOrElse(Nil)
}

object ParameterSymbolBuilderCompanion extends AmfObjectSimpleBuilderCompanion[Parameter] {
  override def getType: Class[_ <: AmfElement] = classOf[Parameter]

  override val supportedIri: String = ParameterModel.`type`.head.iri()

  override def construct(element: Parameter)(implicit ctx: StructureContext): Option[SymbolBuilder[Parameter]] =
    Some(new ParameterSymbolBuilder(element))
}
