package org.mulesoft.als.server.modules.diagnostic

import amf.core.annotations.SourceLocation
import amf.core.model.document.BaseUnit
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.{Platform, UnsupportedUrlScheme}
import amf.core.validation.AMFValidationReport
import amf.internal.environment.Environment
import amf.{ProfileName, ProfileNames}
import org.mulesoft.als.server.client.ClientNotifier
import org.mulesoft.als.server.logger.Logger
import org.mulesoft.als.server.modules.ast.AstListener
import org.mulesoft.als.server.modules.customvalidation.RegisterProfileManager
import org.mulesoft.als.server.workspace.WorkspaceManager
import org.mulesoft.amfintegration.AmfImplicits.BaseUnitImp
import org.mulesoft.amfintegration.AmfResolvedUnit
import org.mulesoft.lsp.feature.telemetry.TelemetryProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomProfileDiagnosticManager(override protected val telemetryProvider: TelemetryProvider,
                                     override protected val clientNotifier: ClientNotifier,
                                     override protected val logger: Logger,
                                     override protected val env: Environment,
                                     override protected val validationGatherer: ValidationGatherer,
                                     val registry: RegisterProfileManager,
                                     val platformSerializer: PlatformSerializer,
                                     val validatorBuilder: ValidatorBuilder,
                                     val platform: Platform)
    extends DiagnosticManager
    with AstListener[AmfResolvedUnit] {

  override protected val managerName: DiagnosticManagerKind = CustomValidationDiagnosticKind

  var workspaceManager: Option[WorkspaceManager] = None

  def withWorkspaceManager(workspaceManager: WorkspaceManager): Unit = this.workspaceManager = Some(workspaceManager)

  /**
    * Called on new AST available
    *
    * @param ast  - AST
    * @param uuid - telemetry UUID
    */
  override def onNewAst(ast: AmfResolvedUnit, uuid: String): Unit = {
    gatherValidationErrors(ast.originalUnit.identifier, ast, uuid)
  }

  override def onRemoveFile(uri: String): Unit = {
    validationGatherer.removeFile(uri, managerName)
    clientNotifier.notifyDiagnostic(AlsPublishDiagnosticsParams(uri, Nil, ProfileNames.AMF))
  }

  private def tree(baseUnit: BaseUnit): Set[String] =
    baseUnit.flatRefs
      .map(bu => bu.location().getOrElse(bu.id))
      .toSet + baseUnit.location().getOrElse(baseUnit.id)

  private def gatherValidationErrors(uri: String, resolved: AmfResolvedUnit, uuid: String): Future[Unit] = {
    val startTime = System.currentTimeMillis()

    for {
      validator      <- validatorBuilder()
      unit           <- resolved.resolvedUnit
      serializedUnit <- platformSerializer.serialize(unit)
      reports <- Future.sequence(registry.profiles.map(uri =>
        readFile(uri, platform, env).map(content => {
          processValidation(validator, content, serializedUnit, unit)
        })))
    } yield {
      val results = reports.flatMap { r =>
        r.results
      }
      results.foreach(println)
      validationGatherer
        .indexNewReport(ErrorsWithTree(uri, results.toSeq, Some(tree(resolved.originalUnit))), managerName, uuid)
      notifyReport(uri,
                   resolved.originalUnit,
                   resolved.diagnosticsBundle,
                   managerName,
                   ProfileName("CustomValidation"))

      val endTime = System.currentTimeMillis()
      this.logger.debug(s"It took ${endTime - startTime} milliseconds to validate with Go env",
                        "CustomValidationDiagnosticManager",
                        "gatherValidationErrors")
    }
  }

  private def processValidation(validator: Validator,
                                content: String,
                                serializedUnit: String,
                                unit: BaseUnit): AMFValidationReport = {
    val r = validator.validate(content, serializedUnit, debug = false)
    println("_________")
    println(r)
    println("_________")
    val report = new OPAValidatorReportLoader().load(r)
    println(report)
    val copied = report.copy(results = report.results.map { r =>
      val element = unit
        .findById(r.targetNode)
      val location = element
        .flatMap(d => d.annotations.find(classOf[SourceLocation]))
        .map(_.location)
      r.copy(location = location)
    })
    copied
  }

  protected def readFile(uri: String, platform: Platform, environment: Environment): Future[String] = {
    try {
      platform
        .fetchContent(uri, AMFPluginsRegistry.obtainStaticConfig().withResourceLoaders(environment.loaders.toList))
        .map { content =>
          content.stream.toString //TODO: option?
        }
    } catch {
      case _: UnsupportedUrlScheme => Future.successful("") // TODO: HANDLE
      case e: Exception            => Future.failed(e)
    }
  }
}
