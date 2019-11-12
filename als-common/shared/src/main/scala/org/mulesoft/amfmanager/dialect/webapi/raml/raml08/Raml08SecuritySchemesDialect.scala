package org.mulesoft.amfmanager.dialect.webapi.raml.raml08

import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.XsdTypes.xsdString
import amf.dialects.RAML08Dialect.DialectNodes
import amf.dialects.RAML08Dialect.DialectNodes.DataTypeNodeId
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}
import amf.plugins.domain.webapi.metamodel.security.{OAuth1SettingsModel, OAuth2SettingsModel, SecuritySchemeModel}
import amf.plugins.domain.webapi.metamodel.{OperationModel, ParameterModel, RequestModel, ResponseModel}
import Raml08TypesDialect.{DialectLocation, ShapeNodeId}

object Raml08SecuritySchemesDialect {

  val shapeTypesProperty: PropertyMapping = PropertyMapping()
    .withId(DialectLocation + "#/declarations/ShapeNode/inherits")
    .withNodePropertyMapping(ShapeModel.Inherits.value.iri())
    .withName("type")
    .withEnum(
      Seq(
        "string",
        "number",
        "integer",
        "boolean",
        "array",
        "file",
        "object",
        "date-only",
        "time-only",
        "datetime-only",
        "datetime",
        "nil",
        "any"
      ))
    .withLiteralRange(xsdString.iri())
    .withObjectRange(Seq(ShapeNodeId))

  val DescribedBy: NodeMapping = NodeMapping()
    .withId(DialectLocation + "#/declarations/DescribedBy")
    .withName("DescribedBy")
    .withNodeTypeMapping((Namespace.Security + "DescribedBy").iri())
    .withPropertiesMapping(Seq(
      PropertyMapping()
        .withId(DialectLocation + "#/declarations/DescribedBy/headers")
        .withName("headers")
        .withNodePropertyMapping(RequestModel.Headers.value.iri())
        .withMapTermKeyProperty(ParameterModel.Name.value.iri())
        .withObjectRange(Seq(
          DataTypeNodeId
        )),
      PropertyMapping()
        .withId(DialectLocation + "#/declarations/DescribedBy/parameters")
        .withName("queryParameters")
        .withNodePropertyMapping(RequestModel.QueryParameters.value.iri())
        .withMapTermKeyProperty(ShapeModel.Name.value.iri())
        .withObjectRange(Seq(
          DataTypeNodeId
        )),
      PropertyMapping()
        .withId(DialectLocation + "#/declarations/DescribedBy/queryString")
        .withName("queryString")
        .withNodePropertyMapping(PropertyShapeModel.`type`.head.iri())
        .withMapTermKeyProperty(ShapeModel.Name.value.iri())
        .withObjectRange(Seq(
          DataTypeNodeId
        )),
      PropertyMapping()
        .withId(DialectLocation + "#/declarations/DescribedBy/responses")
        .withName("responses")
        .withNodePropertyMapping(OperationModel.Responses.value.iri())
        .withMapTermKeyProperty(ResponseModel.StatusCode.value.iri())
        .withObjectRange(Seq(
          DialectNodes.ResponseNode.id
        )),
    ))

  val OAuth1Settings: NodeMapping = NodeMapping()
    .withId(DialectLocation + "#/declarations/OAuth1Settings")
    .withName("OAuth1Settings")
    .withNodeTypeMapping(OAuth1SettingsModel.`type`.head.iri())
    .withPropertiesMapping(Seq(
      PropertyMapping()
        .withId(
          DialectLocation + "#/declarations/OAuth1Settings/requestTokenUri")
        .withName("requestTokenUri")
        .withNodePropertyMapping(
          OAuth1SettingsModel.RequestTokenUri.value.iri())
        .withLiteralRange(xsdString.iri()),
      PropertyMapping()
        .withId(
          DialectLocation + "#/declarations/OAuth1Settings/authorizationUri")
        .withName("authorizationUri")
        .withNodePropertyMapping(
          OAuth1SettingsModel.AuthorizationUri.value.iri())
        .withLiteralRange(xsdString.iri()),
      PropertyMapping()
        .withId(
          DialectLocation + "#/declarations/OAuth1Settings/tokenCredentialsUri")
        .withName("tokenCredentialsUri")
        .withNodePropertyMapping(
          OAuth1SettingsModel.TokenCredentialsUri.value.iri())
        .withLiteralRange(xsdString.iri()),
      PropertyMapping()
        .withId(DialectLocation + "#/declarations/OAuth1Settings/signatures")
        .withName("signatures")
        .withNodePropertyMapping(OAuth1SettingsModel.Signatures.value.iri())
        .withEnum(Seq("HMAC-SHA1", "RSA-SHA1", "PLAINTEXT"))
        .withLiteralRange(xsdString.iri())
    ))

  val OAuth2Settings: NodeMapping = NodeMapping()
    .withId(DialectLocation + "#/declarations/OAuth2Settings")
    .withName("OAuth2Settings")
    .withNodeTypeMapping(OAuth2SettingsModel.`type`.head.iri())
    .withPropertiesMapping(Seq(
      PropertyMapping()
        .withId(
          DialectLocation + "#/declarations/OAuth2Settings/authorizationUri")
        .withName("authorizationUri")
        .withNodePropertyMapping(
          OAuth2SettingsModel.AuthorizationUri.value.iri())
        .withLiteralRange(xsdString.iri()),
      PropertyMapping()
        .withId(
          DialectLocation + "#/declarations/OAuth2Settings/accessTokenUri")
        .withName("accessTokenUri")
        .withNodePropertyMapping(OAuth2SettingsModel.AccessTokenUri.value.iri())
        .withLiteralRange(xsdString.iri()),
      PropertyMapping()
        .withId(
          DialectLocation + "#/declarations/OAuth2Settings/authorizationGrants")
        .withName("authorizationGrants")
        .withNodePropertyMapping(
          OAuth2SettingsModel.AuthorizationGrants.value.iri())
        .withEnum(Seq("code", "token", "owner", "credentials"))
        .withAllowMultiple(true)
        .withLiteralRange(xsdString.iri()),
      PropertyMapping()
        .withId(DialectLocation + "#/declarations/OAuth2Settings/scopes")
        .withName("scopes")
        .withNodePropertyMapping(OAuth2SettingsModel.Scopes.value.iri())
        .withAllowMultiple(true)
        .withLiteralRange(xsdString.iri())
    ))

  val SecurityScheme: NodeMapping = NodeMapping()
    .withId(DialectLocation + "#/declarations/SecuritySchemes")
    .withName("ShapeNode")
    .withNodeTypeMapping(SecuritySchemeModel.`type`.head.iri())
    .withPropertiesMapping(Seq(
      PropertyMapping()
        .withId(DialectLocation + "#/declarations/SecuritySchemes/type")
        .withNodePropertyMapping(SecuritySchemeModel.Type.value.iri())
        .withName("type")
        .withEnum(
          Seq(
            "OAuth 1.0",
            "OAuth 2.0",
            "Basic Authentication",
            "Digest Authentication",
            "x-"
          ))
        .withLiteralRange(xsdString.iri()),
      PropertyMapping()
        .withId(DialectLocation + "#/declarations/SecuritySchemes/description")
        .withNodePropertyMapping(SecuritySchemeModel.Description.value.iri())
        .withName("description")
        .withLiteralRange(xsdString.iri()),
      PropertyMapping()
        .withId(DialectLocation + "#/declarations/SecuritySchemes/describedBy")
        .withNodePropertyMapping((Namespace.Security + "DescribedBy").iri())
        .withName("describedBy")
        .withObjectRange(Seq(DescribedBy.id))
    ))

}