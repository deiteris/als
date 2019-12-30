package org.mulesoft.als.client.convert

import org.mulesoft.als.client.lsp.command.ClientCommand
import org.mulesoft.als.client.lsp.common.{
  ClientLocation,
  ClientLocationLink,
  ClientPosition,
  ClientRange,
  ClientTextDocumentIdentifier,
  ClientTextDocumentItem,
  ClientTextDocumentPositionParams,
  ClientVersionedTextDocumentIdentifier
}
import org.mulesoft.als.client.lsp.configuration.{
  ClientClientCapabilities,
  ClientInitializeParams,
  ClientInitializeResult,
  ClientServerCapabilities,
  ClientStaticRegistrationOptions,
  ClientTextDocumentClientCapabilities,
  ClientWorkspaceClientCapabilities,
  ClientWorkspaceFolder
}
import org.mulesoft.als.client.lsp.edit.{
  ClientCreateFile,
  ClientDeleteFile,
  ClientDeleteFileOptions,
  ClientNewFileOptions,
  ClientRenameFile,
  ClientTextDocumentEdit,
  ClientTextEdit,
  ClientWorkspaceEdit
}
import org.mulesoft.als.client.lsp.feature.completion.{
  ClientCompletionClientCapabilities,
  ClientCompletionContext,
  ClientCompletionItem,
  ClientCompletionItemClientCapabilities,
  ClientCompletionItemKindClientCapabilities,
  ClientCompletionList,
  ClientCompletionOptions,
  ClientCompletionParams
}
import org.mulesoft.als.client.lsp.feature.diagnostic.{
  ClientDiagnostic,
  ClientDiagnosticClientCapabilities,
  ClientDiagnosticRelatedInformation
}
import org.mulesoft.als.client.lsp.feature.documentsymbol.{
  ClientDocumentSymbolClientCapabilities,
  ClientSymbolKindClientCapabilities
}
import org.mulesoft.lsp.command.Command
import org.mulesoft.lsp.common.{
  Location,
  LocationLink,
  Position,
  Range,
  TextDocumentIdentifier,
  TextDocumentItem,
  TextDocumentPositionParams,
  VersionedTextDocumentIdentifier
}
import org.mulesoft.lsp.configuration.{
  ClientCapabilities,
  InitializeParams,
  InitializeResult,
  ServerCapabilities,
  StaticRegistrationOptions,
  TextDocumentClientCapabilities,
  WorkspaceClientCapabilities,
  WorkspaceFolder
}
import org.mulesoft.lsp.edit.{
  CreateFile,
  DeleteFile,
  DeleteFileOptions,
  NewFileOptions,
  RenameFile,
  TextDocumentEdit,
  TextEdit,
  WorkspaceEdit
}
import org.mulesoft.lsp.feature.completion.{
  CompletionClientCapabilities,
  CompletionContext,
  CompletionItem,
  CompletionItemClientCapabilities,
  CompletionItemKindClientCapabilities,
  CompletionList,
  CompletionOptions,
  CompletionParams
}
import org.mulesoft.lsp.feature.diagnostic.{Diagnostic, DiagnosticClientCapabilities, DiagnosticRelatedInformation}
import org.mulesoft.lsp.feature.documentsymbol.{DocumentSymbolClientCapabilities, SymbolKindClientCapabilities}

import scala.language.implicitConversions

object LspConvertersSharedToClient {

  implicit class ClientSymbolKindClientCapabilitiesConverter(v: SymbolKindClientCapabilities) {
    def toClient: ClientSymbolKindClientCapabilities =
      ClientSymbolKindClientCapabilities(v)
  }

  implicit class ClientDocumentSymbolClientCapabilitiesConverter(v: DocumentSymbolClientCapabilities) {
    def toClient: ClientDocumentSymbolClientCapabilities =
      ClientDocumentSymbolClientCapabilities(v)
  }

  implicit class ClientCommandConverter(v: Command) {
    def toClient: ClientCommand =
      ClientCommand(v)
  }

  implicit class ClientLocationLinkConverter(v: LocationLink) {
    def toClient: ClientLocationLink =
      ClientLocationLink(v)
  }

  implicit class ClientPositionConverter(v: Position) {
    def toClient: ClientPosition =
      ClientPosition(v)
  }

  implicit class ClientRangeConverter(v: Range) {
    def toClient: ClientRange =
      ClientRange(v)
  }

  implicit class ClientTextDocumentIdentifierConverter(v: TextDocumentIdentifier) {
    def toClient: ClientTextDocumentIdentifier =
      ClientTextDocumentIdentifier(v)
  }

  implicit class ClientVersionedTextDocumentIdentifierConverter(v: VersionedTextDocumentIdentifier) {
    def toClient: ClientVersionedTextDocumentIdentifier =
      ClientVersionedTextDocumentIdentifier(v)
  }

  implicit class ClientTextDocumentItemConverter(v: TextDocumentItem) {
    def toClient: ClientTextDocumentItem =
      ClientTextDocumentItem(v)
  }

  implicit class ClientTextDocumentPositionParamsConverter(v: TextDocumentPositionParams) {
    def toClient: ClientTextDocumentPositionParams =
      ClientTextDocumentPositionParams(v)
  }

  implicit class ClientTextDocumentClientCapabilitiesConverter(v: TextDocumentClientCapabilities) {
    def toClient: ClientTextDocumentClientCapabilities =
      ClientTextDocumentClientCapabilities(v)
  }

  implicit class ClientWorkspaceClientCapabilitiesConverter(v: WorkspaceClientCapabilities) {
    def toClient: ClientWorkspaceClientCapabilities =
      ClientWorkspaceClientCapabilities(v)
  }

  implicit class ClientClientCapabilitiesConverter(v: ClientCapabilities) {
    def toClient: ClientClientCapabilities =
      ClientClientCapabilities(v)
  }

  implicit class ClientStaticRegistrationOptionsConverter(v: StaticRegistrationOptions) {
    def toClient: ClientStaticRegistrationOptions =
      ClientStaticRegistrationOptions(v)
  }

  implicit class ClientWorkspaceFolderConverter(v: WorkspaceFolder) {
    def toClient: ClientWorkspaceFolder =
      ClientWorkspaceFolder(v)
  }

  implicit class ClientNewFileOptionsConverter(v: NewFileOptions) {
    def toClient: ClientNewFileOptions =
      ClientNewFileOptions(v)
  }

  implicit class ClientCreateFileConverter(v: CreateFile) {
    def toClient: ClientCreateFile =
      ClientCreateFile(v)
  }

  implicit class ClientRenameFileConverter(v: RenameFile) {
    def toClient: ClientRenameFile =
      ClientRenameFile(v)
  }

  implicit class ClientDeleteFileOptionsConverter(v: DeleteFileOptions) {
    def toClient: ClientDeleteFileOptions =
      ClientDeleteFileOptions(v)
  }

  implicit class ClientDeleteFileConverter(v: DeleteFile) {
    def toClient: ClientDeleteFile =
      ClientDeleteFile(v)
  }

  implicit class ClientTextEditConverter(v: TextEdit) {
    def toClient: ClientTextEdit =
      ClientTextEdit(v)
  }

  implicit class ClientTextDocumentEditConverter(v: TextDocumentEdit) {
    def toClient: ClientTextDocumentEdit =
      ClientTextDocumentEdit(v)
  }

  implicit class ClientWorkspaceEditConverter(v: WorkspaceEdit) {
    def toClient: ClientWorkspaceEdit =
      ClientWorkspaceEdit(v)
  }

  implicit class ClientCompletionContextConverter(v: CompletionContext) {
    def toClient: ClientCompletionContext =
      ClientCompletionContext(v)
  }

  implicit class ClientCompletionItemConverter(v: CompletionItem) {
    def toClient: ClientCompletionItem =
      ClientCompletionItem(v)
  }

  implicit class ClientLocationConverter(v: Location) {
    def toClient: ClientLocation =
      ClientLocation(v)
  }

  implicit class ClientDiagnosticClientCapabilitiesConverter(v: DiagnosticClientCapabilities) {
    def toClient: ClientDiagnosticClientCapabilities =
      ClientDiagnosticClientCapabilities(v)
  }

  implicit class ClientDiagnosticRelatedInformationConverter(v: DiagnosticRelatedInformation) {
    def toClient: ClientDiagnosticRelatedInformation =
      ClientDiagnosticRelatedInformation(v)
  }

  implicit class ClientDiagnosticConverter(v: Diagnostic) {
    def toClient: ClientDiagnostic =
      ClientDiagnostic(v)
  }

  implicit class ClientCompletionItemKindClientCapabilitiesConverter(v: CompletionItemKindClientCapabilities) {
    def toClient: ClientCompletionItemKindClientCapabilities =
      ClientCompletionItemKindClientCapabilities(v)
  }

  implicit class ClientCompletionItemClientCapabilitiesConverter(v: CompletionItemClientCapabilities) {
    def toClient: ClientCompletionItemClientCapabilities =
      ClientCompletionItemClientCapabilities(v)
  }

  implicit class ClientCompletionClientCapabilitiesConverter(v: CompletionClientCapabilities) {
    def toClient: ClientCompletionClientCapabilities =
      ClientCompletionClientCapabilities(v)
  }

  implicit class ClientCompletionListConverter(v: CompletionList) {
    def toClient: ClientCompletionList =
      ClientCompletionList(v)
  }

  implicit class ClientCompletionOptionsConverter(v: CompletionOptions) {
    def toClient: ClientCompletionOptions =
      ClientCompletionOptions(v)
  }

  implicit class ClientCompletionParamsConverter(v: CompletionParams) {
    def toClient: ClientCompletionParams =
      ClientCompletionParams(v)
  }

  implicit class ClientInitializeParamsConverter(v: InitializeParams) {
    def toClient: ClientInitializeParams =
      ClientInitializeParams(v)
  }

  implicit class ClientServerCapabilitiesConverter(v: ServerCapabilities) {
    def toClient: ClientServerCapabilities =
      ClientServerCapabilities(v)
  }

  implicit class ClientInitializeResultConverter(v: InitializeResult) {
    def toClient: ClientInitializeResult =
      ClientInitializeResult(v)
  }
}
