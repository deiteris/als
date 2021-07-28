package org.mulesoft.als.server

import org.mulesoft.als.server.feature.serialization.{SerializationParams, SerializationResult}
import org.mulesoft.lsp.feature.RequestType
import org.yaml.builder.DocBuilder

abstract class SerializationProps[S]() {

  def newDocBuilder(): DocBuilder[S]
  val requestType: RequestType[SerializationParams, SerializationResult[S]] =
    new RequestType[SerializationParams, SerializationResult[S]] {}
}
