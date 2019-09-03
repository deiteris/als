package org.mulesoft.als.server.lsp4j

import amf.client.resource.{FileResourceLoader, HttpResourceLoader}
import amf.core.unsafe.PlatformSecrets
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoaderAdapter
import org.mulesoft.als.server.LanguageServerBuilder
import org.mulesoft.als.server.client.ClientNotifier
import org.mulesoft.als.server.logger.Logger
import org.mulesoft.als.server.lsp4j.internal.DefaultJvmDirectoryResolver
import org.mulesoft.als.server.modules.ast.AstManager
import org.mulesoft.als.server.modules.completion.SuggestionsManager
import org.mulesoft.als.server.modules.diagnostic.DiagnosticManager
import org.mulesoft.als.server.modules.structure.StructureManager
import org.mulesoft.als.server.textsync.TextDocumentManager
import org.mulesoft.amfmanager.CustomDialects
import org.mulesoft.lsp.server.LanguageServer

object LanguageServerFactory extends PlatformSecrets {

  def alsLanguageServer(clientNotifier: ClientNotifier,
                        logger: Logger,
                        dialects: Seq[CustomDialects] = Seq()): LanguageServer = {
    val documentManager = new TextDocumentManager(platform, logger)

    val baseEnvironment = Environment()
      .add(ResourceLoaderAdapter(FileResourceLoader()))
      .add(ResourceLoaderAdapter(HttpResourceLoader()))

    val astManager = new AstManager(documentManager, baseEnvironment, platform, logger)
    val completionManager =
      new SuggestionsManager(documentManager,
                             astManager,
                             DefaultJvmDirectoryResolver,
                             platform,
                             baseEnvironment,
                             logger)
    val diagnosticManager = new DiagnosticManager(documentManager, astManager, clientNotifier, platform, logger)
    val structureManager  = new StructureManager(documentManager, astManager, logger, platform)

    LanguageServerBuilder()
      .withTextDocumentSyncConsumer(documentManager)
      .addInitializable(astManager)
      .addInitializable(diagnosticManager)
      .addRequestModule(completionManager)
      .addRequestModule(structureManager)
      .build()
  }
}
