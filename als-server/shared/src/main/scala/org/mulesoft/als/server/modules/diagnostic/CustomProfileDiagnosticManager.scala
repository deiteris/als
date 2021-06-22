package org.mulesoft.als.server.modules.diagnostic

import amf.core.services.RuntimeValidator
import amf.core.validation.AMFValidationReport
import org.mulesoft.als.server.client.ClientNotifier
import org.mulesoft.als.server.logger.Logger
import org.mulesoft.als.server.modules.ast.{BaseUnitListener, BaseUnitListenerParams, ResolvedUnitListener}
import org.mulesoft.lsp.feature.telemetry.{MessageTypes, TelemetryProvider}
import amf.internal.environment.Environment
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance}
import org.mulesoft.als.server.modules.customvalidation.RegisterProfileManager
import org.mulesoft.amfintegration.AmfResolvedUnit

import scala.concurrent.ExecutionContext.Implicits.global

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
    if (registry.profiles.nonEmpty && !(ast.parseResult.baseUnit.isInstanceOf[Dialect] || ast.parseResult.baseUnit
          .isInstanceOf[DialectInstance])) {
      val uri = ast.parseResult.location

      RuntimeValidator(ast.parseResult.baseUnit, registry.profiles.head, resolved = false, env = env)
        .map { vr =>
          val report =
            AMFValidationReport(vr.conforms, vr.model, vr.profile, vr.results ++ ast.parseResult.eh.getErrors)
          validationGatherer
            .indexNewReport(ErrorsWithTree(uri, report.results, Some(ast.parseResult.tree)), managerName, uuid)
          notifyReport(uri, ast.parseResult.baseUnit, ast.diagnosticsBundle, managerName, registry.profiles.head)
        }

    }
  }

  override def onRemoveFile(uri: String): Unit = {}
}
