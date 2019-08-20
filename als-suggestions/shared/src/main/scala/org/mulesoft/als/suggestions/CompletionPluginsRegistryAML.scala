package org.mulesoft.als.suggestions

import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.plugins.aml._
import org.mulesoft.als.suggestions.plugins.aml.webapi.raml.AMLlibraryPathCompletion

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompletionPluginsRegistryAML {

  private val pluginsSet: mutable.Set[AMLCompletionPlugin] = mutable.Set()

  def registerPlugin(plugin: AMLCompletionPlugin): CompletionPluginsRegistryAML = {
    pluginsSet += plugin
    this
  }

  def cleanPlugins(): Unit = pluginsSet.clear()

  def suggests(params: AmlCompletionRequest): Future[Seq[RawSuggestion]] = {
    val seq: Seq[Future[Seq[RawSuggestion]]] = pluginsSet.map(_.resolve(params)).toSeq
    Future.sequence(seq).map(_.flatten)
  }
}

object CompletionsPluginHandler {

  private val registries: mutable.Map[String, CompletionPluginsRegistryAML] = mutable.Map()

  def pluginSuggestions(params: AmlCompletionRequest): Future[Seq[RawSuggestion]] =
    registries.getOrElse(params.actualDialect.id, AMLBaseCompletionPlugins.base).suggests(params)

  def registerPlugin(plugin: AMLCompletionPlugin, dialect: String): Unit = registries.get(dialect) match {
    case Some(registry) => registry.registerPlugin(plugin)
    case _              => registries.put(dialect, new CompletionPluginsRegistryAML().registerPlugin(plugin))
  }

  def registerPlugins(plugins: Seq[AMLCompletionPlugin], dialect: String): Unit = registries.get(dialect) match {
    case Some(registry) => plugins.foreach(registry.registerPlugin)
    case _ =>
      val p = new CompletionPluginsRegistryAML()
      plugins.foreach(p.registerPlugin)
      registries.put(dialect, p)
  }

  def cleanIndex(): Unit = registries.keys.foreach(registries.remove)
}

object AMLBaseCompletionPlugins {
  val all: Seq[AMLCompletionPlugin] = Seq(
    AMLStructureCompletionPlugin,
    AMLEnumCompletionPlugin,
    AMLRootDeclarationsCompletionPlugin,
    AMLRamlStyleDeclarationsReferences,
    AMLKnownValueCompletionPlugin,
    AMLComponentKeyCompletionPlugin,
    AMLRefTagCompletionPlugin,
    AMLPathCompletionPlugin,
    AMLlibraryPathCompletion,
    AMLBooleanPropertyValue,
    AMLJsonSchemaStyleDeclarationReferences
  )

  val base: CompletionPluginsRegistryAML = {
    val b = new CompletionPluginsRegistryAML
    all.foreach(b.registerPlugin)
    b
  }
}
