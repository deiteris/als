package org.mulesoft.als.suggestions.client

import amf.core.model.document.BaseUnit
import amf.core.parser.{Position => AmfPosition}
import amf.core.remote.Platform
import amf.core.unsafe.PlatformSecrets
import amf.internal.environment.Environment
import amf.plugins.document.vocabularies.model.document.Dialect
import org.mulesoft.als.common.dtoTypes.{Position => DtoPosition}
import org.mulesoft.als.common.{DirectoryResolver, EnvironmentPatcher, PlatformDirectoryResolver}
import org.mulesoft.als.suggestions._
import org.mulesoft.als.suggestions.aml.AmlCompletionRequestBuilder
import org.mulesoft.als.suggestions.interfaces.Syntax._
import org.mulesoft.als.suggestions.interfaces.{CompletionProvider, Syntax}
import org.mulesoft.als.suggestions.patcher.{ContentPatcher, PatchedContent}
import org.mulesoft.amfmanager.InitOptions
import org.mulesoft.amfmanager.dialect.DialectKnowledge
import org.mulesoft.lsp.feature.completion.CompletionItem
import org.mulesoft.lsp.server.AmfInstance

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Suggestions(platform: Platform,
                  environment: Environment,
                  directoryResolver: DirectoryResolver,
                  amfInstance: AmfInstance)
    extends SuggestionsHelper {
  def init(options: InitOptions = InitOptions.WebApiProfiles): Future[Unit] =
    Core.init(options, amfInstance)

  def suggest(language: String, url: String, position: Int, snippetsSupport: Boolean): Future[Seq[CompletionItem]] = {

    platform
      .resolve(url, environment)
      .map(content => {
        val originalContent = content.stream.toString
        val (patched, patchedEnv) =
          patchContentInEnvironment(environment, url, originalContent, position)
        (patched, patchedEnv)
      })
      .flatMap {
        case (patchedContent, patchedEnv) =>
          suggestWithPatchedEnvironment(language, url, patchedContent, position, patchedEnv, snippetsSupport)
      }
  }

  def buildProvider(bu: BaseUnit,
                    position: Int,
                    url: String,
                    patchedContent: PatchedContent,
                    snippetSupport: Boolean): Future[CompletionProvider] = {
    DialectKnowledge.dialectFor(bu) match {
      case Some(d) =>
        Future(
          buildCompletionProviderAST(bu,
                                     d,
                                     bu.id,
                                     DtoPosition(position, patchedContent.original),
                                     patchedContent,
                                     snippetSupport))
      case _ if isHeader(position, url, patchedContent.original) =>
        if (!url.toLowerCase().endsWith(".raml"))
          Future(
            HeaderCompletionProviderBuilder
              .build(url, patchedContent.original, DtoPosition(position, patchedContent.original)))
        else
          Future(
            RamlHeaderCompletionProvider
              .build(url, patchedContent.original, DtoPosition(position, patchedContent.original)))
      case _ =>
        Future.failed(new Exception("Cannot find dialect for unit: " + bu.id))
    }
  }

  def buildProviderAsync(unitFuture: Future[BaseUnit],
                         position: Int,
                         url: String,
                         patchedContent: PatchedContent,
                         snippetSupport: Boolean): Future[CompletionProvider] = {
    unitFuture
      .flatMap(buildProvider(_, position, url, patchedContent, snippetSupport))
  }

  private def isHeader(position: Int, url: String, originalContent: String): Boolean =
    !originalContent
      .substring(0, position)
      .replaceAll("^\\{?\\s+", "")
      .contains('\n')

  private def suggestWithPatchedEnvironment(language: String,
                                            url: String,
                                            patchedContent: PatchedContent,
                                            position: Int,
                                            environment: Environment,
                                            snippetsSupport: Boolean): Future[Seq[CompletionItem]] = {

    buildProviderAsync(amfInstance.parserHelper.parse(url, environment).map(_.baseUnit),
                       position,
                       url,
                       patchedContent,
                       snippetsSupport)
      .flatMap(_.suggest())
  }

  private def buildCompletionProviderAST(bu: BaseUnit,
                                         dialect: Dialect,
                                         url: String,
                                         pos: DtoPosition,
                                         patchedContent: PatchedContent,
                                         snippetSupport: Boolean): CompletionProviderAST = {

    val amfPosition: AmfPosition = pos.toAmfPosition
    CompletionProviderAST(
      AmlCompletionRequestBuilder
        .build(bu, amfPosition, dialect, environment, directoryResolver, platform, patchedContent, snippetSupport))
  }
}

object Suggestions extends PlatformSecrets {
  val default = new Suggestions(platform, Environment(), new PlatformDirectoryResolver(platform), AmfInstance.default)
}

trait SuggestionsHelper {

  def amfParse(url: String, amfInstance: AmfInstance, environment: Environment): Future[BaseUnit] =
    amfInstance.parserHelper.parse(url, environment).map(_.baseUnit)

  def getMediaType(originalContent: String): Syntax = {

    val trimmed = originalContent.trim
    if (trimmed.startsWith("{") || trimmed.startsWith("[")) Syntax.JSON
    else Syntax.YAML
  }

  def patchContentInEnvironment(environment: Environment,
                                fileUrl: String,
                                fileContentsStr: String,
                                position: Int): (PatchedContent, Environment) = {

    val patchedContent = ContentPatcher(fileContentsStr, position, YAML).prepareContent()
    val envWithOverride =
      EnvironmentPatcher.patch(environment, fileUrl, patchedContent.content)

    (patchedContent, envWithOverride)
  }
}
