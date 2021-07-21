package org.mulesoft.als.server.modules.diagnostic

import amf.ProfileName
import amf.core.services.RuntimeValidator
import amf.internal.environment.Environment
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance}
import org.mulesoft.als.server.client.ClientNotifier
import org.mulesoft.als.server.logger.Logger
import org.mulesoft.als.server.modules.ast.{BaseUnitListener, BaseUnitListenerParams}
import org.mulesoft.als.server.modules.customvalidation.RegisterProfileManager
import org.mulesoft.lsp.feature.telemetry.TelemetryProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomProfileDiagnosticManager(override protected val telemetryProvider: TelemetryProvider,
                                     override protected val clientNotifier: ClientNotifier,
                                     override protected val logger: Logger,
                                     override protected val env: Environment,
                                     override protected val validationGatherer: ValidationGatherer,
                                     val registry: RegisterProfileManager)
    extends BaseUnitListener
    with DiagnosticManager {
  override protected val managerName: DiagnosticManagerKind = CustomValidationDiagnosticKind

  /**
    * Called on new AST available
    *
    * @param ast  - AST
    * @param uuid - telemetry UUID
    */
  override def onNewAst(ast: BaseUnitListenerParams, uuid: String): Unit = {
    if (!(ast.parseResult.baseUnit.isInstanceOf[Dialect] || ast.parseResult.baseUnit
          .isInstanceOf[DialectInstance])) {
      val uri = ast.parseResult.location

      validationGatherer
        .indexNewReport(ErrorsWithTree(uri, ast.parseResult.eh.getErrors, Some(ast.parseResult.tree)),
                        managerName,
                        uuid)

      Future
        .sequence(registry.profiles.map(RuntimeValidator(ast.parseResult.baseUnit, _, resolved = false, env = env)))
        .map { reports =>
          val errors = reports.flatMap(_.results) ++ ast.parseResult.eh.getErrors
          validationGatherer
            .indexNewReport(ErrorsWithTree(uri, errors, Some(ast.parseResult.tree)), managerName, uuid)
        }
        .andThen {
          case _ =>
            notifyReport(
              uri,
              ast.parseResult.baseUnit,
              ast.diagnosticsBundle,
              managerName,
              registry.profiles.headOption.getOrElse(ProfileName("DEFAULT"))) // not sure what this last profile is for
        }
    }
  }

  override def onRemoveFile(uri: String): Unit = {}
}
