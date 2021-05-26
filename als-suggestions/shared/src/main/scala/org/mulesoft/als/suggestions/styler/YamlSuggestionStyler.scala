package org.mulesoft.als.suggestions.styler

import org.mulesoft.als.suggestions._
import org.mulesoft.als.suggestions.styler.astbuilder.{AstRawBuilder, YamlAstRawBuilder}
import org.yaml.render.{FlowYamlRender, YamlPartRender, YamlRender, YamlRenderOptions}

case class YamlSuggestionStyler(override val params: StylerParams) extends FlowSuggestionRender {

  override protected val useSpaces: Boolean = true

  private def fixPrefix(prefix: String, text: String) =
    if (prefix.isEmpty && text.startsWith(stringIndentation))
      text.stripPrefix(stringIndentation)
    else prefix + text

  override protected def render(options: SuggestionStructure, builder: AstRawBuilder): String = {
    val prefix =
      if (!options.isKey && ((options.isArray && !params.yPartBranch.isInArray) || options.isObject)) // never will suggest object in value as is not key. Suggestions should be empty
        "\n"
      else ""
    val ast         = builder.ast
    val indentation = 0 // We always want to indent relative to the parent node
    val rendered    = yamlRenderer.render(ast, indentation, buildYamlRenderOptions)
    fixPrefix(prefix, fix(builder, rendered))
  }

  def buildYamlRenderOptions: YamlRenderOptions =
    new YamlRenderOptions().withIndentationSize(params.formattingConfiguration.indentationSize)

  def yamlRenderer: YamlPartRender = {
    if (params.yPartBranch.isInFlow) FlowYamlRender
    else YamlRender
  }

  override def astBuilder: RawSuggestion => AstRawBuilder =
    (raw: RawSuggestion) => new YamlAstRawBuilder(raw, false, params.yPartBranch)
}
