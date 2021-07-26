package org.mulesoft.als.server.modules.diagnostic

import amf.{ProfileName, ProfileNames}
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.{Platform, UnsupportedUrlScheme}
import amf.internal.environment.Environment
import org.mulesoft.als.server.client.ClientNotifier
import org.mulesoft.als.server.logger.Logger
import org.mulesoft.als.server.modules.ast.ResolvedUnitListener
import org.mulesoft.als.server.modules.common.reconciler.Runnable
import org.mulesoft.als.server.modules.serialization.SerializationManager
import org.mulesoft.als.server.workspace.WorkspaceManager
import org.mulesoft.als.wasmloader.AMFValidator
import org.mulesoft.amfintegration.AmfImplicits.BaseUnitImp
import org.mulesoft.amfintegration.{AmfInstance, AmfResolvedUnit, DiagnosticsBundle}
import org.mulesoft.lsp.feature.telemetry.{MessageTypes, TelemetryProvider}
import amf.client.parse.AmfGraphParser
import amf.core.annotations.{LexicalInformation, SourceLocation}
import amf.core.model.document.BaseUnit
import amf.core.validation.AMFValidationReport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

class JsCustomValidationDiagnosticManager(override protected val telemetryProvider: TelemetryProvider,
                                          override protected val clientNotifier: ClientNotifier,
                                          override protected val logger: Logger,
                                          override protected val env: Environment,
                                          override protected val validationGatherer: ValidationGatherer,
                                          val serializationManager: SerializationManager[js.Any],
                                          val platform: Platform)
    extends DiagnosticManager
    with ResolvedUnitListener {
  override protected val managerName: DiagnosticManagerKind = CustomDiagnosticKind
  override type RunType = CustomValidationRunnable

  val instance                                   = AmfInstance(env)
  var workspaceManager: Option[WorkspaceManager] = None

  def withWorkspaceManager(workspaceManager: WorkspaceManager): Unit = this.workspaceManager = Some(workspaceManager)

  protected override def runnable(ast: AmfResolvedUnit, uuid: String): RunType =
    new CustomValidationRunnable(ast.originalUnit.identifier, ast, uuid)

  protected override def onFailure(uuid: String, uri: String, exception: Throwable): Unit = {
    logger.error(s"Error on validation: ${exception.toString}", "CustomValidationDiagnosticManager", "newASTAvailable")
    exception.printStackTrace()
    clientNotifier.notifyDiagnostic(ValidationReport(uri, Set.empty, ProfileNames.AMF).publishDiagnosticsParams)
  }

  protected override def onSuccess(uuid: String, uri: String): Unit =
    logger.debug(s"End report: $uuid", "CustomValidationRunnable", "newASTAvailable")

  /**
    * Meant just for logging
    *
    * @param resolved
    * @param uuid
    */
  override protected def onNewAstPreprocess(resolved: AmfResolvedUnit, uuid: String): Unit =
    logger.debug("Running custom validations on:\n" + resolved.originalUnit.id,
                 "CustomValidationDiagnosticManager",
                 "newASTAvailable")

  override def onRemoveFile(uri: String): Unit = {
    validationGatherer.removeFile(uri, managerName)
    clientNotifier.notifyDiagnostic(AlsPublishDiagnosticsParams(uri, Nil, ProfileNames.AMF))
  }

  private def tree(baseUnit: BaseUnit): Set[String] =
    baseUnit.flatRefs
      .map(bu => bu.location().getOrElse(bu.id))
      .toSet + baseUnit.location().getOrElse(baseUnit.id)

  private def gatherValidationErrors(uri: String,
                                     resolved: AmfResolvedUnit,
                                     references: Map[String, DiagnosticsBundle],
                                     uuid: String): Future[Unit] = {
    val startTime = System.currentTimeMillis()
    workspaceManager.get
      .getWorkspace(uri)
      .workspaceConfiguration
      .map(config => {
        for {
          validator      <- AMFValidator()
          unit           <- resolved.resolvedUnit
          serializedUnit <- serializationManager.resolveAndSerialize(unit)
          reports <- Future.sequence(config.validationProfiles.map(uri =>
            readFile(uri, platform, env).map(content => {
              processValidation(validator, content, serializedUnit.result, unit)
            })))
        } yield {
          val results = reports.flatMap { r =>
            r.results
          }
          results.foreach(println)
          validationGatherer
            .indexNewReport(ErrorsWithTree(uri, results.toSeq, Some(tree(resolved.originalUnit))), managerName, uuid)
          notifyReport(uri, resolved.originalUnit, references, managerName, ProfileName("CustomValidation"))

          val endTime = System.currentTimeMillis()
          this.logger.debug(s"It took ${endTime - startTime} milliseconds to validate with Go env",
                            "CustomValidationDiagnosticManager",
                            "gatherValidationErrors")
        }
      })
      .getOrElse(Future.successful())
  }

  private def processValidation(validator: AMFValidator,
                                content: String,
                                serializedUnit: js.Any,
                                unit: BaseUnit): AMFValidationReport = {
    val r = validator.validate(content, JSON.stringify(serializedUnit), debug = false)
    println("_________")
    println(r)
    println("_________")
    val report = new OPAValidatorReportLoader().load(r)
    println(report)
    println("result first: " + report.results.head.toString())
    val copied = report.copy(results = report.results.map { r =>
      val element = unit
        .findById(r.targetNode)
      val maybeInformation = element
        .flatMap(d => d.annotations.find(classOf[LexicalInformation]))
      val location = element
        .flatMap(d => d.annotations.find(classOf[SourceLocation]))
        .map(_.location)
      r.copy(position = maybeInformation, location = location)
    })

    println("copied first: " + copied.results.head.toString())
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
//  private def report(uri: String,
//                     telemetryProvider: TelemetryProvider,
//                     resolved: AmfResolvedUnit,
//                     uuid: String,
//                     profile: ProfileName): Future[AMFValidationReport] = {
//    telemetryProvider.timeProcess(
//      "AMF report",
//      MessageTypes.BEGIN_CUSTOM_DIAGNOSTIC,
//      MessageTypes.END_CUSTOM_DIAGNOSTIC,
//      "ResolutionDiagnosticManager - resolution diagnostics",
//      uri,
//      tryValidationReport(uri, telemetryProvider, resolved, uuid, profile),
//      uuid
//    )
//
//  }

  class CustomValidationRunnable(var uri: String, ast: AmfResolvedUnit, uuid: String) extends Runnable[Unit] {
    private var canceled = false

    private val kind = "CustomValidationRunnable"

    def run(): Promise[Unit] = {
      val promise = Promise[Unit]()

      def innerRunGather() =
        gatherValidationErrors(ast.originalUnit.identifier, ast, ast.diagnosticsBundle, uuid) andThen {
          case Success(report) => promise.success(report)

          case Failure(error) => promise.failure(error)
        }

      telemetryProvider.timeProcess(
        "End report",
        MessageTypes.BEGIN_CUSTOM_DIAGNOSTIC,
        MessageTypes.END_CUSTOM_DIAGNOSTIC,
        "CustomValidationRunnable : onNewAst",
        uri,
        innerRunGather,
        uuid
      )

      promise
    }

    def conflicts(other: Runnable[Any]): Boolean =
      other.asInstanceOf[CustomValidationRunnable].kind == kind && uri == other
        .asInstanceOf[CustomValidationRunnable]
        .uri

    def cancel() {
      canceled = true
    }

    def isCanceled(): Boolean = canceled
  }

}
