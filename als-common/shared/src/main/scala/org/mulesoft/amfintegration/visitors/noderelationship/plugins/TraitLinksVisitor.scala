package org.mulesoft.amfintegration.visitors.noderelationship.plugins

import amf.core.annotations.{SourceAST, SourceNode}
import amf.core.metamodel.domain.LinkableElementModel
import amf.core.metamodel.domain.templates.ParametrizedDeclarationModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.DomainExtension
import amf.core.model.domain.templates.{AbstractDeclaration, ParametrizedDeclaration}
import amf.core.model.domain.{AmfArray, AmfElement}
import amf.core.parser.FieldEntry
import amf.core.vocabulary.Namespace
import amf.plugins.domain.webapi.models.{EndPoint, Operation}
import org.mulesoft.amfintegration.AmfImplicits.AmfAnnotationsImp
import org.mulesoft.amfintegration.relationships.LinkTypes.LinkTypes
import org.mulesoft.amfintegration.relationships.{LinkTypes, RelationshipLink}
import org.mulesoft.amfintegration.visitors.WebApiElementVisitorFactory
import org.mulesoft.amfintegration.visitors.noderelationship.NodeRelationshipVisitorType
import org.yaml.model.{YMapEntry, YPart}

/**
  * @test: org.mulesoft.als.server.modules.definition.files.DefinitionFilesTest - raml-test 1/2
  */
class TraitLinksVisitor extends NodeRelationshipVisitorType {
  override protected def innerVisit(element: AmfElement): Seq[RelationshipLink] =
    element match {
      case o: Operation =>
        extractFromEntriesExtends(o.fields.fields())
      case e: EndPoint =>
        extractFromEntriesExtends(e.fields.fields())
      case e: DomainExtension =>
        extractAnnotations(e)
      case _ => Nil
    }

  private def extractAnnotations(e: DomainExtension) =
    e.annotations
      .ast()
      .map {
        case entry: YMapEntry => entry.key
        case o                => o
      }
      .flatMap(extractFromEntriesDefinedBy(e.fields.fields(), _))
      .toSeq

  private def extractFromEntriesExtends(entries: Iterable[FieldEntry]): Seq[RelationshipLink] =
    entries
      .find(fe => fe.field.value == Namespace.Document + "extends")
      .map(parametrizedDeclarationTargetsWithPosition)
      .getOrElse(Nil)

  private def extractFromEntriesDefinedBy(entries: Iterable[FieldEntry], source: YPart): Option[RelationshipLink] =
    entries
      .find(fe => fe.field.value == Namespace.Document + "definedBy")
      .flatMap(t => t.value.value.annotations.ast().map((t, _)))
      .map(target =>
        RelationshipLink(source, target._2, getName(target._1.value.value), None, LinkTypes.TRAITRESOURCES))

  private def parametrizedDeclarationTargetsWithPosition(fe: FieldEntry): Seq[RelationshipLink] =
    fe.value.value match {
      case array: AmfArray =>
        array.values.flatMap {
          case p: ParametrizedDeclaration =>
            p.fields
              .entry(ParametrizedDeclarationModel.Target)
              .flatMap(
                fe =>
                  fe.value.value match {
                    case a: AbstractDeclaration =>
                      a.fields
                        .entry(LinkableElementModel.Target)
                        .flatMap(fieldEntryToLocation(_, p, LinkTypes.TRAITRESOURCES))
                        .orElse(fieldEntryToLocation(fe, p, LinkTypes.TRAITRESOURCES))
                    case _ => None
                }
              )
          case _ => None
        }
      case _ => Nil
    }

  private def fieldEntryToLocation(fe: FieldEntry,
                                   p: ParametrizedDeclaration,
                                   linkTypes: LinkTypes): Option[RelationshipLink] = {
    val maybeParent = fe.value.value.annotations
      .find(classOf[SourceAST])
      .map(_.ast)
      .collect { case entry: YMapEntry => entry }
    fe.value.value.annotations
      .find(classOf[SourceNode])
      .flatMap { sn =>
        p.name
          .annotations()
          .ast()
          .map { sourceEntry =>
            RelationshipLink(
              sourceEntry,
              maybeParent.getOrElse(sn.node),
              getName(fe.value.value),
              None,
              linkTypes
            )
          }
      }
  }
}

object TraitLinksVisitor extends WebApiElementVisitorFactory {
  override def apply(bu: BaseUnit): Option[TraitLinksVisitor] =
    if (applies(bu))
      Some(new TraitLinksVisitor())
    else None
}
