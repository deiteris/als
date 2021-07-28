package org.mulesoft.als.server

import java.io.StringWriter

import org.mulesoft.als.server.client.AlsClientNotifier
import org.mulesoft.als.server.feature.serialization.{SerializationResult, SerializationParams}
import org.mulesoft.als.server.feature.workspace.FilesInProjectParams
import org.mulesoft.als.server.lsp4j.extension.JvmSerializationRequestType
import org.mulesoft.lsp.feature.RequestType
import org.yaml.builder.{DocBuilder, JsonOutputBuilder}

// Default
case class JvmSerializationProps() extends SerializationProps[StringWriter]() {
  override def newDocBuilder(): DocBuilder[StringWriter] = JsonOutputBuilder()

  override val requestType: RequestType[SerializationParams, SerializationResult[StringWriter]] =
    JvmSerializationRequestType
}

object EmptyJvmSerializationProps extends JvmSerializationProps() {}

object EmptyJvmClientNotifier extends AlsClientNotifier[StringWriter] {
  override def notifyProjectFiles(params: FilesInProjectParams): Unit = {}

  override def notifySerialization(params: SerializationResult[StringWriter]): Unit = {}
}
