package org.mulesoft.als.server.lsp4j

import java.util
import java.util.concurrent.CompletableFuture

import org.eclipse.lsp4j
import org.eclipse.lsp4j.jsonrpc.messages
import org.eclipse.lsp4j.{
  CodeAction,
  CodeActionParams,
  Command,
  CompletionItem,
  CompletionList,
  CompletionParams,
  DefinitionParams,
  DidChangeTextDocumentParams,
  DidCloseTextDocumentParams,
  DidOpenTextDocumentParams,
  DidSaveTextDocumentParams,
  DocumentHighlight,
  DocumentHighlightParams,
  DocumentLink,
  DocumentLinkParams,
  DocumentSymbol,
  DocumentSymbolParams,
  Hover,
  ImplementationParams,
  Location,
  LocationLink,
  PrepareRenameParams,
  PublishDiagnosticsParams,
  ReferenceParams,
  RenameParams,
  SymbolInformation,
  TypeDefinitionParams,
  WorkspaceEdit
}
import org.mulesoft.als.server.custom.{CustomEvents, CustomTextDocumentService}
import org.mulesoft.als.server.feature.diagnostic.CleanDiagnosticTreeRequestType
import org.mulesoft.als.server.feature.fileusage.FileUsageRequestType
import org.mulesoft.als.server.feature.serialization.ConversionRequestType
import org.mulesoft.als.server.lsp4j.AlsJConversions._
import org.mulesoft.als.server.lsp4j.LspConversions._
import org.mulesoft.als.server.lsp4j.extension._
import org.mulesoft.als.server.protocol.LanguageServer
import org.mulesoft.als.server.protocol.textsync.{AlsTextDocumentSyncConsumer, DidFocusParams}
import org.mulesoft.lsp.Lsp4JConversions._
import org.mulesoft.lsp.LspConversions._
import org.mulesoft.lsp.feature.codeactions.CodeActionRequestType
import org.mulesoft.lsp.feature.completion.CompletionRequestType
import org.mulesoft.lsp.feature.definition.DefinitionRequestType
import org.mulesoft.lsp.feature.documentsymbol.DocumentSymbolRequestType
import org.mulesoft.lsp.feature.hover.HoverRequestType
import org.mulesoft.lsp.feature.highlight.DocumentHighlightRequestType
import org.mulesoft.lsp.feature.implementation.ImplementationRequestType
import org.mulesoft.lsp.feature.link.DocumentLinkRequestType
import org.mulesoft.lsp.feature.reference.ReferenceRequestType
import org.mulesoft.lsp.feature.rename.{PrepareRenameRequestType, RenameRequestType}
import org.mulesoft.lsp.feature.typedefinition.TypeDefinitionRequestType
import org.mulesoft.lsp.feature.{RequestHandler, RequestType}

import scala.concurrent.ExecutionContext.Implicits.global

class TextDocumentServiceImpl(private val inner: LanguageServer) extends CustomTextDocumentService with CustomEvents {

  private val textDocumentSyncConsumer: AlsTextDocumentSyncConsumer =
    inner.textDocumentSyncConsumer

  override def didOpen(params: DidOpenTextDocumentParams): Unit =
    textDocumentSyncConsumer.didOpen(params)

  override def didChange(params: DidChangeTextDocumentParams): Unit =
    textDocumentSyncConsumer.didChange(params)

  override def didClose(params: DidCloseTextDocumentParams): Unit =
    textDocumentSyncConsumer.didClose(params)

  override def didSave(params: DidSaveTextDocumentParams): Unit = {}

  override def didFocus(params: DidFocusParams): Unit =
    textDocumentSyncConsumer.didFocus(params)

  private def resolveHandler[P, R](`type`: RequestType[P, R]): RequestHandler[P, R] = {
    val maybeHandler = inner.resolveHandler(`type`)
    if (maybeHandler.isEmpty) throw new UnsupportedOperationException
    else maybeHandler.get
  }

  override def references(params: ReferenceParams): CompletableFuture[util.List[_ <: Location]] =
    javaFuture(resolveHandler(ReferenceRequestType)(params), lsp4JLocations)

  override def definition(params: DefinitionParams)
    : CompletableFuture[messages.Either[util.List[_ <: Location], util.List[_ <: LocationLink]]] =
    javaFuture(resolveHandler(DefinitionRequestType)(params), lsp4JLocationsEither)

  override def implementation(params: ImplementationParams)
    : CompletableFuture[messages.Either[util.List[_ <: Location], util.List[_ <: LocationLink]]] =
    javaFuture(resolveHandler(ImplementationRequestType)(params), lsp4JLocationsEither)

  override def typeDefinition(params: TypeDefinitionParams)
    : CompletableFuture[messages.Either[util.List[_ <: Location], util.List[_ <: LocationLink]]] =
    javaFuture(resolveHandler(TypeDefinitionRequestType)(params), lsp4JLocationsEither)

  override def completion(
      params: CompletionParams): CompletableFuture[messages.Either[util.List[CompletionItem], CompletionList]] =
    javaFuture(resolveHandler(CompletionRequestType)(params), lsp4JCompletionEither)

  override def rename(params: RenameParams): CompletableFuture[WorkspaceEdit] =
    javaFuture(resolveHandler(RenameRequestType)(params), lsp4JWorkspaceEdit)

  override def prepareRename(
      params: PrepareRenameParams): CompletableFuture[messages.Either[lsp4j.Range, lsp4j.PrepareRenameResult]] =
    javaFuture(resolveHandler(PrepareRenameRequestType)(params), lsp4JOptionEitherRangeWithPlaceholder)

  override def documentSymbol(
      params: DocumentSymbolParams): CompletableFuture[util.List[messages.Either[SymbolInformation, DocumentSymbol]]] =
    javaFuture(resolveHandler(DocumentSymbolRequestType)(params), lsp4JDocumentSymbolsResult)

  override def codeAction(
      params: CodeActionParams): CompletableFuture[util.List[messages.Either[Command, CodeAction]]] =
    javaFuture(resolveHandler(CodeActionRequestType)(params), lsp4JCodeActionResult)

  override def documentLink(params: DocumentLinkParams): CompletableFuture[util.List[DocumentLink]] =
    javaFuture(resolveHandler(DocumentLinkRequestType)(params), lsp4JDocumentLinkRequestResult)

  override def documentHighlight(
      params: DocumentHighlightParams): CompletableFuture[util.List[_ <: DocumentHighlight]] =
    javaFuture(resolveHandler(DocumentHighlightRequestType)(params), lsp4JDocumentHighlights)

  override def conversion(params: ConversionParams): CompletableFuture[SerializedDocument] =
    javaFuture(resolveHandler(ConversionRequestType)(params), serializedDocument)

  override def cleanDiagnosticTree(
      params: CleanDiagnosticTreeParams): CompletableFuture[util.List[PublishDiagnosticsParams]] = {
    javaFuture(resolveHandler(CleanDiagnosticTreeRequestType)(params), lsp4JPublishDiagnosticsParamsSeq)
  }

  override def fileUsage(params: lsp4j.TextDocumentIdentifier): CompletableFuture[util.List[Location]] = {
    javaFuture(resolveHandler(FileUsageRequestType)(params), lsp4JLocations)
  }

  override def serialization(params: SerializationParams): CompletableFuture[SerializedDocument] = {
    javaFuture(resolveHandler(JvmSerializationRequestType)(params), serializationSerializedDocument)
  }

  override def hover(params: lsp4j.HoverParams): CompletableFuture[Hover] = {
    javaFuture(resolveHandler(HoverRequestType)(params), lsp4JHoverParams)
  }
}
