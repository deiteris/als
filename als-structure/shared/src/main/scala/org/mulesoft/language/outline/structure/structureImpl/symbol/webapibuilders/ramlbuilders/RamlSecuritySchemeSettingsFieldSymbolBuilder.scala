package org.mulesoft.language.outline.structure.structureImpl.symbol.webapibuilders.ramlbuilders

import amf.core.model.domain.AmfObject
import amf.core.parser.FieldEntry
import amf.plugins.domain.webapi.metamodel.security.SecuritySchemeModel
import org.mulesoft.language.outline.structure.structureImpl.StructureContext
import org.mulesoft.language.outline.structure.structureImpl.symbol.builders.fieldbuilders.{
  ObjectFieldTypeSymbolBuilderCompanion,
  SingleObjectFieldSymbolBuilder
}
import org.mulesoft.language.outline.structure.structureImpl.symbol.builders.{
  FieldTypeSymbolBuilder,
  IriFieldSymbolBuilderCompanion
}

class RamlSecuritySchemeSettingsFieldSymbolBuilder(override val value: AmfObject, override val element: FieldEntry)(
    override implicit val ctx: StructureContext)
    extends SingleObjectFieldSymbolBuilder {
  override protected val name: String = "settings"
}

object RamlSecuritySchemeSettingsFieldSymbolBuilderCompanion
    extends ObjectFieldTypeSymbolBuilderCompanion
    with IriFieldSymbolBuilderCompanion {

  override val supportedIri: String = SecuritySchemeModel.Settings.value.iri()

  override def construct(element: FieldEntry, value: AmfObject)(
      implicit ctx: StructureContext): Option[FieldTypeSymbolBuilder[AmfObject]] =
    Some(new RamlSecuritySchemeSettingsFieldSymbolBuilder(value, element))
}
