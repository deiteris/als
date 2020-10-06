package org.mulesoft.als.suggestions.aml

import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.{BaseUnit, EncodesModel}
import amf.core.model.domain.{AmfObject, DomainElement}
import amf.core.parser.{FieldEntry, Position => AmfPosition}
import amf.core.remote.Platform
import amf.internal.environment.Environment
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}
import org.mulesoft.als.common._
import org.mulesoft.als.common.dtoTypes.{PositionRange, TextHelper, Position => DtoPosition}
import org.mulesoft.als.configuration.AlsConfigurationReader
import org.mulesoft.als.suggestions.CompletionsPluginHandler
import org.mulesoft.als.suggestions.aml.declarations.DeclarationProvider
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.patcher.PatchedContent
import org.mulesoft.als.suggestions.styler.{SuggestionRender, SuggestionStylerBuilder}
import org.mulesoft.amfintegration.AmfImplicits._
import org.yaml.model.YNode.MutRef
import org.yaml.model.{YDocument, YNode, YSequence, YType}
class AmlCompletionRequest(val baseUnit: BaseUnit,
                           val position: DtoPosition,
                           val actualDialect: Dialect,
                           val environment: Environment,
                           val directoryResolver: DirectoryResolver,
                           val platform: Platform,
                           val styler: SuggestionRender,
                           val yPartBranch: YPartBranch,
                           val configurationReader: AlsConfigurationReader,
                           private val objectInTree: ObjectInTree,
                           val inheritedProvider: Option[DeclarationProvider] = None,
                           val rootUri: Option[String],
                           val completionsPluginHandler: CompletionsPluginHandler) {

  lazy val branchStack: Seq[AmfObject] = objectInTree.stack

  lazy val amfObject: AmfObject = objectInTree.obj

  private val currentNode = DialectNodeFinder.find(objectInTree.obj, None, actualDialect)

  private def entryAndMapping: Option[(FieldEntry, Boolean)] = {
    objectInTree.fieldValue
      .map(fe => (fe, false))
      .orElse({
        FieldEntrySearcher(objectInTree.obj, currentNode, yPartBranch, actualDialect)
          .search(objectInTree.stack.headOption)
      })
  }

  lazy val (fieldEntry: Option[FieldEntry], isKeyMapping: Boolean) = entryAndMapping match {
    case Some(value) => (Some(value._1), value._2)
    case None        => (None, false)
  }

  def prefix: String = styler.params.prefix

  val propertyMapping: List[PropertyMapping] = {

    val mappings: List[PropertyMapping] = currentNode match {
      case Some(nm: NodeMapping) =>
        PropertyMappingFilter(objectInTree, actualDialect, nm).filter().toList
      case _ => Nil
    }

    fieldEntry match {
      case Some(e) =>
        val maybeMappings = mappings
          .find(
            pm =>
              pm.fields
                .fields()
                .exists(f => f.value.toString == e.field.value.iri()))
          .map(List(_))
        maybeMappings
          .getOrElse(mappings)
      case _ => mappings
    }
  }

  lazy val declarationProvider: DeclarationProvider = {
    inheritedProvider.getOrElse(DeclarationProvider(baseUnit, Some(actualDialect)))
  }
}

// todo: make instance
object AmlCompletionRequestBuilder {

  private def indentation(bu: BaseUnit, position: DtoPosition): Int =
    bu.raw
      .flatMap(text => {
        val pos  = position
        val left = text.substring(0, pos.offset(text))
        val line =
          if (left.contains("\n"))
            left.substring(left.lastIndexOf("\n")).stripPrefix("\n")
          else left
        val first = line.headOption match {
          case Some(c) if c == ' ' || c == '\t' => Some(c)
          case _                                => None
        }
        first.map(f => {
          line.substring(0, line.takeWhile(_ == f).length)
        })
      })
      .getOrElse("")
      .length

  def build(baseUnit: BaseUnit,
            position: AmfPosition,
            dialect: Dialect,
            environment: Environment,
            directoryResolver: DirectoryResolver,
            platform: Platform,
            patchedContent: PatchedContent,
            snippetSupport: Boolean,
            rootLocation: Option[String],
            configuration: AlsConfigurationReader,
            completionsPluginHandler: CompletionsPluginHandler): AmlCompletionRequest = {
    val yPartBranch: YPartBranch = {
      val ast = baseUnit.ast match {
        case Some(d: YDocument) => d
        case Some(p)            => YDocument(IndexedSeq(p), p.sourceName)
        case None               => YDocument(IndexedSeq.empty, "")
      }
      NodeBranchBuilder
        .build(ast, position, YamlUtils.isJson(baseUnit))
    }

    val dtoPosition = DtoPosition(position)
    val styler = SuggestionStylerBuilder.build(
      !yPartBranch.isJson,
      prefix(yPartBranch, dtoPosition, patchedContent.original),
      patchedContent,
      dtoPosition,
      yPartBranch,
      configuration,
      snippetSupport,
      if (baseUnit.location().isDefined) platform.mimeFromExtension(platform.extension(baseUnit.location().get).get)
      else None,
      indentation(baseUnit, dtoPosition)
    )

    new AmlCompletionRequest(
      baseUnit,
      dtoPosition,
      dialect,
      environment,
      directoryResolver,
      platform,
      styler,
      yPartBranch,
      configuration,
      objInTree(baseUnit, position, dialect),
      rootUri = rootLocation,
      completionsPluginHandler = completionsPluginHandler
    )
  }

  private def objInTree(baseUnit: BaseUnit, position: AmfPosition, definedBy: Dialect): ObjectInTree = {
    val objectInTree = ObjectInTreeBuilder.fromUnit(baseUnit, position, baseUnit.identifier, definedBy)
    objectInTree.obj match {
      case d: EncodesModel if d.fields.exists(DocumentModel.Encodes) =>
        ObjectInTree(d.encodes, Seq(objectInTree.obj) ++ objectInTree.stack, position)
      case _ => objectInTree
    }
  }

  private def prefix(yPartBranch: YPartBranch, position: DtoPosition, content: String): String = {
    yPartBranch.node match {
      case node: MutRef =>
        node.origValue.toString.substring(
          0,
          (0 max (position.column - node.origValue.range.columnFrom - {
            if (node.asScalar.exists(_.mark.plain)) 0 else 1 // if there is a quotation mark, adjust the range according
          })) min node.origValue.toString.length
        )
      case node: YNode =>
        if (PositionRange(node.tag.range).contains(position))
          node.tag.text
        else
          node.tagType match {
            case YType.Str =>
              val lines: Iterator[String] = node
                .as[String]
                .linesIterator
                .drop(position.line - node.range.lineFrom)
              if (lines.hasNext) {
                val next = lines.next()
                val diff = position.column - node.range.columnFrom - {
                  if (node.asScalar.exists(_.mark.plain)) 0 else 1
                } // if there is a quotation mark, adjust the range according

                next.substring(0, Math.max(diff, 0))
              } else ""
            case _ => ""
          }
      case _: YSequence => ""
      case _ =>
        val line        = TextHelper.linesWithSeparators(content)(position.line)
        val textContent = line.substring(0, position.toAmfPosition.column)
        // add trailing space to avoid checking if it ends with `{, [, `, "`
        s"$textContent ".split(Array('{', '[', ''', '"')).lastOption.getOrElse("").trim
    }
  }

  def forElement(element: DomainElement,
                 current: DomainElement,
                 filterProvider: DeclarationProvider,
                 parent: AmlCompletionRequest,
                 ignoredPlugins: Set[AMLCompletionPlugin]): AmlCompletionRequest = {
    val currentIndex = parent.branchStack.indexOf(current) + 1
    val newStack: Seq[AmfObject] =
      if (currentIndex < parent.branchStack.length) parent.branchStack.splitAt(currentIndex)._2 else parent.branchStack
    val objectInTree =
      ObjectInTreeBuilder.fromSubTree(element,
                                      parent.position.toAmfPosition,
                                      parent.baseUnit.identifier,
                                      newStack,
                                      parent.actualDialect)
    new AmlCompletionRequest(
      parent.baseUnit,
      parent.position,
      parent.actualDialect,
      parent.environment,
      parent.directoryResolver,
      parent.platform,
      parent.styler,
      parent.yPartBranch,
      parent.configurationReader,
      objectInTree,
      Some(filterProvider),
      parent.rootUri,
      parent.completionsPluginHandler.filter(ignoredPlugins)
    )
  }
}
