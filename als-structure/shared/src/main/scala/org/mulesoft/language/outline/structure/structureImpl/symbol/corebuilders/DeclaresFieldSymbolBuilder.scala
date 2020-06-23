package org.mulesoft.language.outline.structure.structureImpl.symbol.corebuilders

import amf.core.metamodel.document.DocumentModel
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.{AmfArray, AmfObject, Shape}
import amf.core.parser.FieldEntry
import org.mulesoft.amfintegration.AmfImplicits._
import org.mulesoft.language.outline.structure.structureImpl._
import org.mulesoft.language.outline.structure.structureImpl.symbol.builders.fieldbuilders.{
  ArrayFieldTypeSymbolBuilder,
  ArrayFieldTypeSymbolBuilderCompanion
}
import org.mulesoft.language.outline.structure.structureImpl.symbol.builders.{
  FieldTypeSymbolBuilder,
  IriFieldSymbolBuilderCompanion,
  SymbolBuilder
}

class DeclaresFieldSymbolBuilder(override val value: AmfArray, override val element: FieldEntry)(
    override implicit val ctx: StructureContext)
    extends ArrayFieldTypeSymbolBuilder {

  private lazy val terms: Map[String, String] = ctx.dialect.declarationsMapTerms

  private val groupedDeclarations: Map[String, Seq[AmfObject]] = value.values
    .collect({ case obj: AmfObject if obj.location().contains(ctx.location) => obj })
    .groupBy(declarationName)

  private def getMeta(obj: AmfObject): String =
    obj match {
      case _: Shape => ShapeModel.`type`.head.iri()
      case _        => obj.metaURIs.head
    }

  protected def builderFor(obj: AmfObject): Option[SymbolBuilder[_]] = ctx.factory.builderFor(obj)

  private def buildSymbol(name: String, elements: Seq[AmfObject]): Option[DocumentSymbol] = {
    val children: List[DocumentSymbol] = elements
      .flatMap(o => builderFor(o).map(_.build()).getOrElse(Nil))
      .sortWith((ds1, ds2) => ds1.range.start < ds2.range.start)
      .toList
    children match {
      case Nil => None
      case head :: tail =>
        Some(
          DocumentSymbol(name,
                         head.kind,
                         deprecated = false,
                         head.range + tail.lastOption.getOrElse(head).range,
                         head.selectionRange,
                         children))
    }
  }

  protected def declarationName(obj: AmfObject): String = terms.getOrElse(getMeta(obj), "unknown")

  override def build(): Seq[DocumentSymbol] =
    groupedDeclarations.flatMap { case (name, elements) => buildSymbol(name, elements) }.toSeq
  override protected val optionName: Option[String] = None
}

object DeclaresFieldSymbolBuilderCompanion
    extends ArrayFieldTypeSymbolBuilderCompanion
    with IriFieldSymbolBuilderCompanion {
  override def construct(element: FieldEntry, value: AmfArray)(
      implicit ctx: StructureContext): Option[FieldTypeSymbolBuilder[AmfArray]] =
    Some(new DeclaresFieldSymbolBuilder(value, element))

  override val supportedIri: String = DocumentModel.Declares.value.iri()
}
