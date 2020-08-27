package org.mulesoft.als.actions.codeactions

import amf.core.model.document.BaseUnit
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.vocabularies.model.document.Dialect
import org.mulesoft.als.actions.codeactions.plugins.base.{CodeActionFactory, CodeActionRequestParams}
import org.mulesoft.als.common.{ObjectInTreeBuilder, WorkspaceEditSerializer}
import org.mulesoft.als.common.cache.UnitWithCaches
import org.mulesoft.als.common.diff.FileAssertionTest
import org.mulesoft.als.common.dtoTypes.PositionRange
import org.mulesoft.als.configuration.AlsConfiguration
import org.mulesoft.amfintegration.{AmfInstance, AmfParseResult, ParserHelper}
import org.mulesoft.lsp.edit.{TextEdit, WorkspaceEdit}
import org.mulesoft.lsp.feature.codeactions.CodeAction
import org.mulesoft.lsp.feature.common.Position
import org.mulesoft.lsp.feature.telemetry.MessageTypes.MessageTypes
import org.mulesoft.lsp.feature.telemetry.TelemetryProvider
import org.scalatest.{Assertion, AsyncFlatSpec, Matchers}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YMap}
import org.yaml.render.YamlRender

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait BaseCodeActionTests extends AsyncFlatSpec with Matchers with FileAssertionTest {
  override val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  protected def relativeUri(element: String): String =
    s"file://als-actions/shared/src/test/resources/codeactions/$element"

  private def assertCodeActions(result: Seq[CodeAction], goldenPath: String): Future[Assertion] = {
    val value1   = result.headOption.flatMap(_.edit)
    val expected = WorkspaceEditSerializer(value1.getOrElse(WorkspaceEdit.empty)).serialize()

    for {
      tmp <- writeTemporaryFile(goldenPath)(expected)
      r   <- assertDifferences(tmp, goldenPath)
    } yield r
  }

  protected def runTest(elementUri: String,
                        range: PositionRange,
                        dialect: Option[Dialect],
                        pluginFactory: CodeActionFactory): Future[Assertion] =
    for {
      params <- buildParameter(elementUri, range, dialect)
      result <- {
        val plugin = pluginFactory(params)
        plugin.isApplicable should be(true)
        plugin.run(params)
      }
      r <- assertCodeActions(result, relativeUri(elementUri + ".golden.yaml"))
    } yield r

  protected def runTestNotApplicable(elementUri: String,
                                     range: PositionRange,
                                     dialect: Option[Dialect],
                                     pluginFactory: CodeActionFactory): Future[Assertion] =
    for {
      params <- buildParameter(elementUri, range, dialect)
    } yield pluginFactory(params).isApplicable should be(false)

  protected def parseElement(elementUri: String): Future[AmfParseResult] =
    new ParserHelper(platform, AmfInstance.default)
      .parse(relativeUri(elementUri))

  protected def buildParameter(elementUri: String,
                               range: PositionRange,
                               dialect: Option[Dialect]): Future[CodeActionRequestParams] =
    parseElement(elementUri)
      .map(bu => DummyCompilableUnit(bu.baseUnit))
      .map { cu =>
        CodeActionRequestParams(cu.unit.location().getOrElse(relativeUri(elementUri)),
                                range,
                                cu.unit,
                                cu.tree,
                                cu.yPartBranch,
                                dialect,
                                AlsConfiguration(),
                                dummyTelemetryProvider,
                                "")
      }

  protected val dummyTelemetryProvider: TelemetryProvider =
    (_: String, _: MessageTypes, _: String, _: String, _: String) => {
      /* do nothing */
    }
}

case class DummyCompilableUnit(unit: BaseUnit) extends UnitWithCaches
