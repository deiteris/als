package org.mulesoft.als.actions.codeactions.plugins.testaction

import org.mulesoft.als.actions.codeactions.plugins.base.{
  CodeActionFactory,
  CodeActionRequestParams,
  CodeActionResponsePlugin
}
import org.mulesoft.als.common.dtoTypes.Position
import org.mulesoft.lsp.edit.WorkspaceEdit
import org.mulesoft.lsp.feature.codeactions.{CodeAction, CodeActionKind}
import org.mulesoft.lsp.feature.codeactions.CodeActionKind.CodeActionKind
import org.mulesoft.lsp.feature.telemetry.MessageTypes.{BEGIN_TEST_ACTION, END_TEST_ACTION, MessageTypes}
import org.mulesoft.lsp.feature.telemetry.TelemetryProvider

import scala.concurrent.Future

case class TestCodeAction(params: CodeActionRequestParams, override val kind: CodeActionKind)
    extends CodeActionResponsePlugin {
  val isApplicable: Boolean =
    params.range.start == params.range.`end` && params.range.start == Position(0, 0)

  override protected def telemetry: TelemetryProvider = params.telemetryProvider

  override protected def task(params: CodeActionRequestParams): Future[Seq[CodeAction]] = Future.successful {
    Seq(CodeAction("test action", Some(kind), None, Some(false), Some(WorkspaceEdit.empty), None))
  }

  override protected def code(params: CodeActionRequestParams): String = "test code action"

  override protected def beginType(params: CodeActionRequestParams): MessageTypes = BEGIN_TEST_ACTION

  override protected def endType(params: CodeActionRequestParams): MessageTypes = END_TEST_ACTION

  override protected def msg(params: CodeActionRequestParams): String =
    s"If you are seeing this, the code action communication is working\n\t${params.uri}\t${params.range}"

  override protected def uri(params: CodeActionRequestParams): String = params.uri
}

object TestCodeAction extends CodeActionFactory {
  override val kind: CodeActionKind = CodeActionKind.Test

  override def apply(params: CodeActionRequestParams): CodeActionResponsePlugin = TestCodeAction(params, kind)
}