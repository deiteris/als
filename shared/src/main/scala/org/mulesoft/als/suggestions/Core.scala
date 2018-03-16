package org.mulesoft.als.suggestions

import org.mulesoft.als.suggestions.interfaces.Syntax
import org.mulesoft.als.suggestions.interfaces.Syntax._
import org.mulesoft.als.suggestions.plugins.StructureCompletionPlugin
import org.mulesoft.als.suggestions.plugins.oas.DefinitionReferenceCompletionPlugin
import org.mulesoft.als.suggestions.plugins.raml.TemplateReferencesCompletionPlugin

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

object Core {

    def init():Future[Unit] =
        org.mulesoft.high.level.Core.init()
        .map(x=>{
            //both RAML and OAS
            CompletionPluginsRegistry.registerPlugin(StructureCompletionPlugin())

            //RAML only
            CompletionPluginsRegistry.registerPlugin(TemplateReferencesCompletionPlugin())

            //OAS only
            CompletionPluginsRegistry.registerPlugin(DefinitionReferenceCompletionPlugin())
        })

    def prepareText(text:String, offset:Int, syntax:Syntax):String = {
        syntax match {
            case YAML => CompletionProvider.prepareYamlContent(text,offset)
            case _ => throw new Error(s"Syntax not supported: $syntax")
        }
    }
}
