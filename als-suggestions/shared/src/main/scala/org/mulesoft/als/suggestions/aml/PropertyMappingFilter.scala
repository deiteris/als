package org.mulesoft.als.suggestions.aml

import amf.core.VendorExtensionCompiler
import amf.core.metamodel.domain.common.{NameFieldSchema, NameFieldShacl}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AmfObject
import amf.core.parser.FieldEntry
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.model.domain.{DialectDomainElement, NodeMapping, PropertyMapping}
import org.mulesoft.als.common.ObjectInTree
import org.mulesoft.amfintegration.AmfImplicits._
import org.mulesoft.amfintegration.AmfInstance

case class PropertyMappingFilter(objectInTree: ObjectInTree, actualDialect: Dialect, nm: NodeMapping) {

  private def parentTermKey(): Seq[PropertyMapping] =
    objectInTree.stack.headOption
      .flatMap(DialectNodeFinder.find(_, None, actualDialect))
      .collectFirst({ case n: NodeMapping => n })
      .map(_.propertiesMapping())
      .getOrElse(Nil)
      .filter(p => p.mapTermKeyProperty().option().isDefined)

  private def isInDeclarations(nm: NodeMapping): Boolean =
    actualDialect
      .documents()
      .root()
      .declaredNodes()
      .exists(_.mappedNode().option().contains(nm.id)) && objectInTree.stack.last.isInstanceOf[BaseUnit]

  private val semanticNameIris: Seq[String] = Seq(NameFieldSchema.Name.value.iri(), NameFieldShacl.Name.value.iri())

  private def filterSemanticsName(props: Seq[PropertyMapping]) =
    if (isInDeclarations(nm)) props.filterNot(p => semanticNameIris.contains(p.nodePropertyMapping().value()))
    else props

  private def filterNonExplicit(props: Seq[PropertyMapping]): Seq[PropertyMapping] =
    props.filterNot(_.name().isNullOrEmpty)

  private def parentMappingsForTerm(nm: NodeMapping) =
    parentTermKey()
      .find(pr => pr.objectRange().exists(or => or.value() == nm.id))
      .map(p => Seq(p.mapTermKeyProperty().option(), p.mapTermValueProperty().option()).flatten)
      .getOrElse(Nil)

  private def filterForTerms(nm: NodeMapping): Seq[PropertyMapping] = {
    val terms = parentMappingsForTerm(nm)
    if (terms.nonEmpty)
      nm.propertiesMapping()
        .filter(p => !p.nodePropertyMapping().option().exists(terms.contains))
    else nm.propertiesMapping()
  }

  def filter(): Seq[PropertyMapping] = filterNonExplicit(filterSemanticsName(filterForTerms(nm)))

}

object DialectNodeFinder {

  def find(amfObject: AmfObject, fieldEntry: Option[FieldEntry], actualDialect: Dialect): Option[NodeMapping] = {
    amfObject.metaURIs
      .flatMap { v =>
        actualDialect.declares.find {
          case s: NodeMapping =>
            s.nodetypeMapping.value() == v && containsAllFields(s, fieldEntry)
          case _ => false
        }
      }
      .collectFirst({ case nm: NodeMapping => nm })
      .orElse(findById(amfObject, fieldEntry, actualDialect))
  }

  private def containsAllFields(nodeMapping: NodeMapping, fieldEntry: Option[FieldEntry]): Boolean = {
    fieldEntry.forall(f => {
      nodeMapping
        .propertiesMapping()
        .find(
          pm =>
            pm.fields
              .fields()
              .exists(_.value.toString == f.field.value.iri()))
        .exists(_.mapTermKeyProperty().isNullOrEmpty)
    })
  }

  /**
    * Handles the case where the AML definition has no classTerm defined
    */
  private def findById(amfObject: AmfObject,
                       fieldEntry: Option[FieldEntry],
                       actualDialect: Dialect): Option[NodeMapping] = {
    amfObject.metaURIs
      .flatMap { v =>
        actualDialect.declares.find {
          case s: NodeMapping =>
            s.id == v && containsAllFields(s, fieldEntry)
          case _ => false
        }
      }
      .collectFirst({ case nm: NodeMapping => nm })
  }

  def find(objInTree: ObjectInTree,
           fieldEntry: Option[FieldEntry],
           actualDialect: Dialect,
           amfInstance: AmfInstance): Option[NodeMapping] = {
    val defaultMapping = objInTree.obj.metaURIs
      .flatMap { v =>
        actualDialect.declares.find {
          case s: NodeMapping =>
            s.nodetypeMapping.value() == v &&
              fieldEntry.forall(f => {
                s.propertiesMapping()
                  .find(pm =>
                    pm.fields
                      .fields()
                      .exists(_.value.toString == f.field.value.iri()))
                  .exists(_.mapTermKeyProperty().isNullOrEmpty)
              })
          case _ => false
        }
      }
      .collectFirst({ case nm: NodeMapping => nm })
    if (defaultMapping.isEmpty) checkSemanticExtensions(objInTree, fieldEntry, amfInstance)
    else defaultMapping
  }

  private def checkSemanticExtensions(objectInTree: ObjectInTree,
                                      fieldEntry: Option[FieldEntry],
                                      amfInstance: AmfInstance): Option[NodeMapping] = {
    if (objectInTree.obj.isInstanceOf[DialectDomainElement] && objectInTree.stack.exists(_.extendedFields.nonEmpty)) {
      objectInTree.stack.find(_.extendedFields.nonEmpty) match {
        case Some(p) =>
          val jumped: Seq[AmfObject] = objectInTree.stack.splitAt(objectInTree.stack.indexOf(p))._1
          val value =
            if (jumped.isEmpty) objectInTree.obj
            else jumped.last

          p.extendedFields.fields().find(_.value.value == value) match {
            case Some(FieldEntry(field, value)) =>
              amfInstance.alsAmlPlugin.registry
                .vendorExtensionForId(field.value.iri())
                .map(_._3)
                .flatMap(d => find(objectInTree.obj, fieldEntry, d))
            case _ => None
          }
        case _ => None
      }
    } else None
  }

  def find(metaUri: String, actualDialect: Dialect): Option[NodeMapping] =
    actualDialect.declares
      .find {
        case s: NodeMapping =>
          s.nodetypeMapping.value() == metaUri
        case _ => false
      }
      .collectFirst({ case nm: NodeMapping => nm })
}
