package org.mulesoft.amfintegration.dialect.dialects.oas.nodes

import amf.core.vocabulary.Namespace.XsdTypes.{amlAnyNode, xsdString}
import org.mulesoft.amfintegration.dialect.dialects.oas.OAS20Dialect.DialectLocation
import org.mulesoft.amfintegration.dialect.dialects.oas.{OAS30Dialect, OasBaseDialect}
import amf.plugins.document.vocabularies.model.domain.PropertyMapping
import amf.plugins.domain.shapes.metamodel.ExampleModel

object AMLExampleObject extends DialectNode {

  override def location: String = OasBaseDialect.DialectLocation

  override def name: String            = "ExampleObject"
  override def nodeTypeMapping: String = ExampleModel.`type`.head.iri()
  override def properties: Seq[PropertyMapping] = Seq(
    PropertyMapping()
      .withId(DialectLocation + "#/declarations/ExampleObject/mediaType")
      .withNodePropertyMapping(ExampleModel.MediaType.value.iri())
      .withName("mediaType")
      .withLiteralRange(xsdString.iri())
  )
}

object Oas30ExampleObject extends DialectNode {

  override def location: String = OAS30Dialect.DialectLocation

  override def name: String            = "ExampleObject"
  override def nodeTypeMapping: String = ExampleModel.`type`.head.iri()
  override def properties: Seq[PropertyMapping] = Seq(
    PropertyMapping()
      .withId(DialectLocation + "#/declarations/ExampleObject/summary")
      .withNodePropertyMapping(ExampleModel.Summary.value.iri())
      .withName("mediaType")
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(DialectLocation + "#/declarations/ExampleObject/description")
      .withNodePropertyMapping(ExampleModel.Description.value.iri())
      .withName("description")
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(DialectLocation + "#/declarations/ExampleObject/value")
      .withNodePropertyMapping(ExampleModel.Raw.value.iri())
      .withName("value")
      .withLiteralRange(amlAnyNode.iri()),
    PropertyMapping()
      .withId(DialectLocation + "#/declarations/ExampleObject/externalValue")
      .withNodePropertyMapping(ExampleModel.ExternalValue.value.iri())
      .withName("externalValue")
      .withLiteralRange(xsdString.iri())
  )
}
