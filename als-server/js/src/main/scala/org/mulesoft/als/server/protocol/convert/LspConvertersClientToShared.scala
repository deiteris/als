package org.mulesoft.als.server.protocol.convert

import org.mulesoft.als.server.protocol.configuration.{AlsClientCapabilities, AlsInitializeParams, AlsInitializeResult, AlsServerCapabilities, ClientAlsClientCapabilities, ClientAlsInitializeParams, ClientAlsInitializeResult, ClientAlsServerCapabilities, ClientCleanDiagnosticTreeClientCapabilities, ClientCleanDiagnosticTreeOptions, ClientSerializationClientCapabilities, ClientSerializationServerOptions}
import org.mulesoft.als.server.protocol.textsync.{ClientDidFocusParams, ClientIndexDialectParams, DidFocusParams, IndexDialectParams}
import org.mulesoft.lsp.configuration.TraceKind
import org.mulesoft.lsp.feature.diagnostic.{CleanDiagnosticTreeClientCapabilities, CleanDiagnosticTreeOptions}
import org.mulesoft.lsp.feature.serialization.{SerializationClientCapabilities, SerializationServerOptions}
import org.mulesoft.lsp.textsync.{ClientTextDocumentSyncOptions, TextDocumentSyncKind}
import org.mulesoft.lsp.convert.LspConvertersClientToShared.{ClientWorkspaceServerCapabilitiesConverter, TextDocumentClientCapabilitiesConverter, TextDocumentSyncOptionsConverter, WorkspaceClientCapabilitiesConverter, WorkspaceFolderConverter, CompletionOptionsConverter}

object LspConvertersClientToShared {
  // $COVERAGE-OFF$ Incompatibility between scoverage and scalaJS

  implicit class ClientSerializationServerOptionsConverter(v: ClientSerializationServerOptions) {
    def toShared: SerializationServerOptions =
      SerializationServerOptions(v.supportsSerialization)
  }

  implicit class ClientCleanDiagnosticTreeOptionsConverter(v: ClientCleanDiagnosticTreeOptions) {
    def toShared: CleanDiagnosticTreeOptions =
      CleanDiagnosticTreeOptions(v.supported)
  }

  implicit class AlsClientCapabilitiesConverter(v: ClientAlsClientCapabilities){
    def toShared: AlsClientCapabilities = AlsClientCapabilities(
      v.workspace.map(_.toShared).toOption,
      v.textDocument.map(_.toShared).toOption,
      v.experimental.toOption,
      serialization = v.serialization.map(_.toShared).toOption,
      cleanDiagnosticTree = v.cleanDiagnosticTree.map(_.toShared).toOption)
  }

  implicit class InitializeParamsConverter(v: ClientAlsInitializeParams) {
    def toShared: AlsInitializeParams =
      AlsInitializeParams(
        Option(v.capabilities).map(c => new AlsClientCapabilitiesConverter(c).toShared),
        v.trace.toOption.map(TraceKind.withName),
        v.rootUri.toOption,
        Option(v.processId),
        Option(v.workspaceFolders).map(_.map(_.toShared).toSeq),
        v.rootPath.toOption,
        v.initializationOptions.toOption,
      )
  }

  implicit class SerializationClientCapabilitiesConverter(v: ClientSerializationClientCapabilities) {
    def toShared: SerializationClientCapabilities = {
      SerializationClientCapabilities(v.acceptsNotification)
    }
  }

  implicit class CleanDiagnosticTreeClientCapabilitiesConverter(v: ClientCleanDiagnosticTreeClientCapabilities) {
    def toShared: CleanDiagnosticTreeClientCapabilities = {
      CleanDiagnosticTreeClientCapabilities(v.enableCleanDiagnostic)
    }
  }

  implicit class ServerCapabilitiesConverter(v: ClientAlsServerCapabilities) {
    def toShared: AlsServerCapabilities =
      AlsServerCapabilities(
        v.textDocumentSync.toOption.map((textDocumentSync: Any) => textDocumentSync match {
          case value: Int => Left(TextDocumentSyncKind(value))
          case _ => Right(textDocumentSync.asInstanceOf[ClientTextDocumentSyncOptions].toShared)
        }),
        v.completionProvider.toOption.map(_.toShared),
        v.definitionProvider,
        v.referencesProvider,
        v.documentSymbolProvider,
        None,
        None,
        None,
        v.workspace.toOption.map(_.toShared),
        v.experimental.toOption,
        v.serialization.toOption.map(_.toShared),
        v.cleanDiagnostics.toOption.map(_.toShared)
      )
  }

  implicit class InitializeResultConverter(v: ClientAlsInitializeResult) {
    def toShared: AlsInitializeResult =
      AlsInitializeResult(v.capabilities.toShared)
  }

  implicit class DidFocusParamsConverter(v: ClientDidFocusParams) {
    def toShared: DidFocusParams =
      DidFocusParams(v.uri, v.version)
  }

  implicit class IndexDialectParamsConverter(v: ClientIndexDialectParams) {
    def toShared: IndexDialectParams =
      IndexDialectParams(v.uri, v.content.toOption)
  }
  // $COVERAGE-ON
}