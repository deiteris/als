package org.mulesoft.als.server

import org.mulesoft.als.server.client.AlsClientNotifier
import org.mulesoft.als.server.feature.serialization.{SerializationParams, SerializationResult}
import org.mulesoft.lsp.feature.RequestType
import org.yaml.builder.DocBuilder

import scala.scalajs.js

abstract class SerializationProps[S](val alsClientNotifier: AlsClientNotifier[S]) {

  def newDocBuilder(): DocBuilder[S]
  val requestType: RequestType[SerializationParams, SerializationResult[S]] =
    new RequestType[SerializationParams, SerializationResult[S]] {}
}

abstract class JsSerializationProp[S](override val alsClientNotifier: AlsClientNotifier[S])
    extends SerializationProps[S](alsClientNotifier) {
  def newDocBuilder2(): DocBuilder[_]
}
