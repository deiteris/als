package org.mulesoft.language.outline.structure.structureImpl.symbol.builders

import amf.core.model.domain.AmfElement
import amf.core.parser.{FieldEntry, Range}
import org.mulesoft.amfintegration.AmfImplicits.AmfAnnotationsImp
import org.mulesoft.language.outline.structure.structureImpl.StructureContext

trait FieldTypeSymbolBuilder[ElementType <: AmfElement] extends FieldSymbolBuilder {
  val value: ElementType

  override protected def range: Option[Range] =
    element.value.annotations.range().orElse(value.annotations.range())
}

trait FieldTypeSymbolBuilderCompanion[ElementType <: AmfElement] extends FieldSymbolBuilderCompanion {

  def getElementType: Class[_ <: AmfElement]
  def construct(element: FieldEntry, value: ElementType)(
      implicit ctx: StructureContext): Option[FieldTypeSymbolBuilder[ElementType]]

  final override def construct(element: FieldEntry)(
      implicit ctx: StructureContext): Option[SymbolBuilder[FieldEntry]] = {
    if (getElementType.isInstance(element.value.value)) {
      construct(element, element.value.value.asInstanceOf[ElementType])
    } else None
  }
}
