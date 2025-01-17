package org.mulesoft.als.server.modules.workspace

import org.mulesoft.amfintegration.relationships.{AliasInfo, RelationshipLink}
import org.mulesoft.amfintegration.visitors.{AmfElementDefaultVisitors, AmfElementVisitors}
import org.mulesoft.lsp.feature.link.DocumentLink

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private[workspace] object Relationships {
  def apply(repository: WorkspaceParserRepository, fcu: () => Option[Future[CompilableUnit]]): Relationships =
    new Relationships(repository, fcu)
}

class Relationships private (private val repository: WorkspaceParserRepository,
                             fcu: () => Option[Future[CompilableUnit]]) {

  private def getVisitorResult[T](uri: String)(fromTree: () => Seq[T],
                                               fallBack: AmfElementVisitors => Seq[T]): Future[Seq[T]] = {
    if (repository.inTree(uri))
      Future.successful(fromTree())
    else
      fcu()
        .map {
          _.map { cu => // todo: optimize in cases in which I want all references from the same BU?
            val visitors = AmfElementDefaultVisitors.build(cu.unit)
            visitors.applyAmfVisitors(cu.unit)
            fallBack(visitors)
          }
        }
        .getOrElse(Future.successful(Nil))
  }

  def getDocumentLinks(uri: String): Future[Seq[DocumentLink]] =
    getVisitorResult(uri)(() => repository.documentLinks().getOrElse(uri, Nil),
                          visitors => visitors.getDocumentLinksFromVisitors.getOrElse(uri, Nil))

  /**
    * Provides Project links for all files
    * @return
    */
  def getAllDocumentLinks: Future[Map[String, Seq[DocumentLink]]] =
    Future.successful(repository.documentLinks())

  def getAliases(uri: String): Future[Seq[AliasInfo]] =
    getVisitorResult(uri)(repository.aliases, visitors => visitors.getAliasesFromVisitors)

  def getRelationships(uri: String): Future[Seq[RelationshipLink]] =
    getVisitorResult(uri)(repository.relationships, visitors => visitors.getRelationshipsFromVisitors)
}
