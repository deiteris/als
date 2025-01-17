package org.mulesoft.amfintegration.dialect.dialects.oas.nodes

import amf.core.vocabulary.Namespace.XsdTypes.xsdString
import amf.plugins.document.vocabularies.model.domain.PropertyMapping
import amf.plugins.domain.webapi.metamodel.{EncodingModel, PayloadModel}
import org.mulesoft.amfintegration.dialect.dialects.oas.OAS20Dialect.DialectLocation
import org.mulesoft.amfintegration.dialect.dialects.oas.OAS30Dialect

object AMLContentObject extends DialectNode with Oas30ExampleProperty {

  override def location: String        = OAS30Dialect.DialectLocation
  override def name: String            = "ContentObject"
  override def nodeTypeMapping: String = PayloadModel.`type`.head.iri()
  override def properties: Seq[PropertyMapping] = Seq(
    PropertyMapping()
      .withId(DialectLocation + "#/declarations/ContentObject/mediaType")
      .withName("mediaType")
      .withNodePropertyMapping(PayloadModel.MediaType.value.iri())
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(DialectLocation + "#/declarations/ContentObject/encoding")
      .withName("encoding")
      .withNodePropertyMapping(PayloadModel.Encoding.value.iri())
      .withMapTermKeyProperty(EncodingModel.PropertyName.value.iri())
      .withObjectRange(Seq(AMLEncodingObject.id)),
    PropertyMapping()
      .withId(DialectLocation + "#/declarations/ContentObject/schema")
      .withName("schema")
      .withNodePropertyMapping(PayloadModel.Schema.value.iri())
      .withObjectRange(Seq(Oas30SchemaObject.id)),
    example,
    examples
  )
}
