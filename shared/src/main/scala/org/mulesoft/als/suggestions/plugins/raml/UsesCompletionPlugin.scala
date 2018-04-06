package org.mulesoft.als.suggestions.plugins.raml

import amf.core.remote.{Raml10, Vendor}
import org.mulesoft.als.suggestions.implementation.{PathCompletion, Suggestion}
import org.mulesoft.als.suggestions.interfaces.{ICompletionPlugin, ICompletionRequest, ISuggestion}
import org.yaml.model.YScalar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

class UsesCompletionPlugin extends ICompletionPlugin {

  override def id: String = UsesCompletionPlugin.ID;

  override def languages: Seq[Vendor] = UsesCompletionPlugin.supportedLanguages;

  override def isApplicable(request:ICompletionRequest): Boolean = request.config.astProvider match {
    case Some(astProvider) => {
      val result = languages.indexOf(astProvider.language) >= 0 && isUses(request)
      result
    };

    case _ => false;
  }

  override def suggest(request: ICompletionRequest): Future[Seq[ISuggestion]] = {

    val baseDir = request.astNode.get.astUnit.project.rootPath

    val relativePath = request.actualYamlLocation.get.value.get.yPart.asInstanceOf[YScalar].text

    if (!relativePath.endsWith(request.prefix)) {

      Promise.successful(Seq[ISuggestion]()).future
    } else {

      val diff = relativePath.length - request.prefix.length

      PathCompletion.complete(baseDir, relativePath, request.config.fsProvider.get)
        .map(paths=>{
          paths.map(path=>{

            val pathStartingWithPrefix = if(diff != 0) path.substring(diff) else path

            Suggestion(pathStartingWithPrefix, id,
              pathStartingWithPrefix, request.prefix)
          })
        })
    }
  }

  def isUses(request: ICompletionRequest): Boolean = {
    request.astNode.get.isElement &&
      request.astNode.get.asElement.get.definition.isAssignableFrom("LibraryBase") &&
      request.actualYamlLocation.isDefined &&
      request.actualYamlLocation.get.parentStack.length >= 3 &&
      request.actualYamlLocation.get.parentStack(2).keyValue.isDefined &&
      request.actualYamlLocation.get.parentStack(2).keyValue.get.yPart.isInstanceOf[YScalar] &&
      request.actualYamlLocation.get.parentStack(2).keyValue.get.yPart.asInstanceOf[YScalar].text == "uses" &&
      request.actualYamlLocation.get.value.isDefined &&
      request.actualYamlLocation.get.value.get.yPart.isInstanceOf[YScalar]

  }
}

object UsesCompletionPlugin {
  val ID = "uses.completion";

  val supportedLanguages: List[Vendor] = List(Raml10);

  def apply(): UsesCompletionPlugin = new UsesCompletionPlugin();
}