package org.mulesoft.als.server.modules.actions

import org.mulesoft.als.common.dtoTypes.Position
import org.mulesoft.als.server.RequestModule
import org.mulesoft.als.server.logger.Logger
import org.mulesoft.als.server.modules.ast.AstManager
import org.mulesoft.lsp.ConfigType
import org.mulesoft.lsp.common.Location
import org.mulesoft.lsp.feature.RequestHandler
import org.mulesoft.lsp.feature.reference.{
  ReferenceClientCapabilities,
  ReferenceConfigType,
  ReferenceParams,
  ReferenceRequestType
}
import org.mulesoft.lsp.feature.telemetry.TelemetryProvider

import scala.concurrent.Future

class FindReferenceManager(val astManager: AstManager,
                           private val telemetryProvider: TelemetryProvider,
                           private val logger: Logger)
    extends RequestModule[ReferenceClientCapabilities, Unit] {

  private var conf: Option[ReferenceClientCapabilities] = None

  override val `type`: ConfigType[ReferenceClientCapabilities, Unit] =
    ReferenceConfigType

  override val getRequestHandlers: Seq[RequestHandler[_, _]] = Seq(
    new RequestHandler[ReferenceParams, Seq[Location]] {
      override def `type`: ReferenceRequestType.type = ReferenceRequestType

      override def apply(params: ReferenceParams): Future[Seq[Location]] =
        findReference(params.textDocument.uri, Position(params.position.line, params.position.character))
    }
  )

  override def applyConfig(config: Option[ReferenceClientCapabilities]): Unit = {
    conf = config
  }

  val onFindReference: (String, Position) => Future[Seq[Location]] = findReference

  def findReference(str: String, position: Position): Future[Seq[Location]] =
    ??? // Future.successful(actionsManager.actions.map(_.getReferences).getOrElse(Nil))

  override def initialize(): Future[Unit] =
    Future.successful()

}