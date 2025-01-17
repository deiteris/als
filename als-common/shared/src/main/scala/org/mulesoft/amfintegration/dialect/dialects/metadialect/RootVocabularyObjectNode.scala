package org.mulesoft.amfintegration.dialect.dialects.metadialect

import amf.core.vocabulary.Namespace.XsdTypes.{xsdString, xsdUri}
import amf.plugins.document.vocabularies.metamodel.document.VocabularyModel
import amf.plugins.document.vocabularies.model.domain.PropertyMapping
import org.mulesoft.amfintegration.dialect.dialects.oas.nodes.DialectNode

object RootVocabularyObjectNode extends DialectNode {
  override def name: String = "RootVocabularyEncodedNode"

  override def nodeTypeMapping: String =
    VocabularyModel.`type`.head.iri()

  override def properties: Seq[PropertyMapping] = Seq(
    PropertyMapping()
      .withId(location + s"#/declarations/$name/vocabulary")
      .withNodePropertyMapping(VocabularyModel.Name.value.iri())
      .withName("vocabulary")
      .withLiteralRange(xsdString.iri())
      .withMinCount(1),
    PropertyMapping()
      .withId(location + s"#/declarations/$name/base")
      .withNodePropertyMapping(VocabularyModel.Base.value.iri())
      .withName("base")
      .withLiteralRange(xsdUri.iri())
      .withMinCount(1),
    PropertyMapping()
      .withId(location + s"#/declarations/$name/external")
      .withNodePropertyMapping(VocabularyModel.References.value.iri())
      .withName("external")
      .withObjectRange(Seq(ExternalObjectNode.id))
      .withMapKeyProperty("name")
      .withMapValueProperty("value"),
    PropertyMapping()
      .withId(location + s"#/declaration/$name/usage")
      .withNodePropertyMapping(VocabularyModel.Usage.value.iri())
      .withName("usage")
      .withLiteralRange(xsdString.iri())
  )
}
