package org.mulesoft.als.suggestions.client

import amf.core.model.document.BaseUnit
import amf.core.parser.{Position => AmfPosition}
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import amf.internal.environment.Environment
import amf.plugins.document.vocabularies.model.document.Dialect
import org.mulesoft.als.common.dtoTypes.{Position => DtoPosition}
import org.mulesoft.als.common.{DirectoryResolver, PlatformDirectoryResolver}
import org.mulesoft.als.configuration.{AlsConfiguration, AlsConfigurationReader}
import org.mulesoft.als.suggestions._
import org.mulesoft.als.suggestions.aml.webapi._
import org.mulesoft.als.suggestions.aml.{
  AmlCompletionRequestBuilder,
  MetaDialectPluginRegistry,
  VocabularyDialectPluginRegistry
}
import org.mulesoft.als.suggestions.interfaces.CompletionProvider
import org.mulesoft.amfintegration.dialect.dialects.ExternalFragmentDialect
import org.mulesoft.amfintegration.{AmfInstance, InitOptions}
import org.mulesoft.lsp.feature.completion.CompletionItem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Suggestions(platform: Platform,
                  environment: Environment,
                  configuration: AlsConfigurationReader,
                  directoryResolver: DirectoryResolver,
                  amfInstance: AmfInstance) {

  // header plugin static?
  val completionsPluginHandler = new CompletionsPluginHandler()

  def initialized(options: InitOptions = InitOptions.AllProfiles): this.type = {
    completionsPluginHandler.cleanIndex()
    HeaderBaseCompletionPlugins.initAll() // TODO: inside OAS CPR?
    if (options.contains(Oas30) || options.contains(Oas))
      Oas30CompletionPluginRegistry.init(amfInstance, completionsPluginHandler)
    if (options.contains(Oas20) || options.contains(Oas))
      Oas20CompletionPluginRegistry.init(amfInstance, completionsPluginHandler)
    if (options.contains(Raml10) || options.contains(Raml))
      RamlCompletionPluginRegistry.init(amfInstance, completionsPluginHandler)
    if (options.contains(Raml08) || options.contains(Raml))
      Raml08CompletionPluginRegistry.init(amfInstance, completionsPluginHandler)
    if (options.contains(AsyncApi20) || options.contains(AsyncApi))
      AsyncApiCompletionPluginRegistry.init(amfInstance, completionsPluginHandler)
    if (options.contains(Aml)) {
      MetaDialectPluginRegistry.init(amfInstance, completionsPluginHandler)
      VocabularyDialectPluginRegistry.init(amfInstance, completionsPluginHandler)
    }

    this
  }

  def suggest(url: String,
              position: Int,
              snippetsSupport: Boolean,
              originalText: String,
              rootLocation: Option[String]): Future[Seq[CompletionItem]] = {
    buildProviderAsync(
      amfInstance
        .modelBuilder()
        .parse(url, environment)
        .map(pr => (pr.baseUnit, pr.definedBy)), // todo: this should receive workspace and get last unit
      originalText,
      position,
      url,
      snippetsSupport,
      rootLocation
    ).flatMap(_.suggest())
  }

  def buildProvider(result: (BaseUnit, Dialect),
                    position: Int,
                    url: String,
                    originalText: String,
                    snippetSupport: Boolean,
                    rootLocation: Option[String]): CompletionProvider = {
    result._2 match {
      case ExternalFragmentDialect.dialect if isHeader(position, originalText) =>
        if (!url.toLowerCase().endsWith(".raml"))
          HeaderCompletionProviderBuilder
            .build(url, originalText, DtoPosition(position, originalText), amfInstance, configuration)
        else
          RamlHeaderCompletionProvider
            .build(url, originalText, DtoPosition(position, originalText))
      case _ =>
        buildCompletionProviderAST(result._1,
                                   result._2,
                                   DtoPosition(position, originalText),
                                   snippetSupport,
                                   rootLocation)
    }
  }

  def buildProviderAsync(unitFuture: Future[(BaseUnit, Dialect)],
                         originalText: String,
                         position: Int,
                         url: String,
                         snippetSupport: Boolean,
                         rootLocation: Option[String]): Future[CompletionProvider] = {
    unitFuture
      .map(buildProvider(_, position, url, originalText, snippetSupport, rootLocation))
  }

  private def isHeader(position: Int, originalContent: String): Boolean =
    !originalContent
      .substring(0, position)
      .replaceAll("^\\{?\\s+", "")
      .contains('\n')

  private def buildCompletionProviderAST(bu: BaseUnit,
                                         dialect: Dialect,
                                         pos: DtoPosition,
                                         snippetSupport: Boolean,
                                         rootLocation: Option[String]): CompletionProviderAST = {

    val amfPosition: AmfPosition = pos.toAmfPosition
    CompletionProviderAST(
      AmlCompletionRequestBuilder
        .build(
          bu,
          amfPosition,
          dialect,
          environment,
          directoryResolver,
          platform,
          bu.raw.getOrElse(""),
          snippetSupport,
          rootLocation,
          configuration,
          completionsPluginHandler,
          amfInstance
        ))
  }
}

object Suggestions extends PlatformSecrets {
  def default =
    new Suggestions(platform,
                    Environment(),
                    AlsConfiguration(),
                    new PlatformDirectoryResolver(platform),
                    AmfInstance.default)
}
