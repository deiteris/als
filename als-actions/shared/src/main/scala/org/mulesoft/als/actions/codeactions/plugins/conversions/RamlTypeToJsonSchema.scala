package org.mulesoft.als.actions.codeactions.plugins.conversions

import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.Annotations
import amf.core.remote.Vendor
import amf.plugins.domain.shapes.models.AnyShape
import org.mulesoft.als.actions.codeactions.plugins.base.{
  CodeActionFactory,
  CodeActionRequestParams,
  CodeActionResponsePlugin
}
import org.mulesoft.als.actions.codeactions.plugins.declarations.common.FileExtractor
import org.mulesoft.als.common.edits.codeaction.AbstractCodeAction
import org.mulesoft.lsp.edit.TextEdit
import org.mulesoft.lsp.feature.common.{Position, Range}
import org.mulesoft.lsp.feature.telemetry.MessageTypes.{
  BEGIN_TYPE_TO_JSON_SCHEMA_ACTION,
  END_TYPE_TO_JSON_SCHEMA_ACTION,
  MessageTypes
}
import org.mulesoft.lsp.feature.telemetry.TelemetryProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RamlTypeToJsonSchema(override protected val params: CodeActionRequestParams)
    extends CodeActionResponsePlugin
    with FileExtractor
    with AmfObjectResolver {

  override protected val fallbackName: String               = "json-schema"
  override protected val extension: String                  = "json"
  override protected val additionalAnnotations: Annotations = Annotations()

  protected def jsonSchemaTextEdit(shape: AnyShape): Future[(String, TextEdit)] =
    for {
      r   <- renderJsonSchema(shape)
      uri <- wholeUri
    } yield (uri, TextEdit(Range(Position(0, 0), Position(0, 0)), r))

  private def renderJsonSchema(shape: AnyShape): Future[String] = Future {
    shape.buildJsonSchema()
  }

  def inProperty: Boolean =
    maybeTree.exists(_.stack.exists(_.isInstanceOf[PropertyShape]))

  override lazy val isApplicable: Boolean =
    params.bu.sourceVendor.contains(Vendor.RAML10) && !inProperty &&
      maybeAnyShape.isDefined && (positionIsExtracted || maybeAnyShape.exists(isInlinedJsonSchema))

  protected def telemetry: TelemetryProvider = params.telemetryProvider

  override protected def code(params: CodeActionRequestParams): String =
    "Raml type to json schema code action"

  override protected def beginType(params: CodeActionRequestParams): MessageTypes =
    BEGIN_TYPE_TO_JSON_SCHEMA_ACTION

  override protected def endType(params: CodeActionRequestParams): MessageTypes =
    END_TYPE_TO_JSON_SCHEMA_ACTION

  override protected def msg(params: CodeActionRequestParams): String =
    s"RAML type to json schema : \n\t${params.uri}\t${params.range}"

  override protected def uri(params: CodeActionRequestParams): String =
    params.uri

  override protected def task(params: CodeActionRequestParams): Future[Seq[AbstractCodeAction]] =
    linkEntry.flatMap { mle =>
      {
        (mle, maybeAnyShape) match {
          case (Some(le), Some(shape)) =>
            jsonSchemaTextEdit(shape).map(edits =>
              buildFileEdit(params.uri, le, edits._1, edits._2).map(RamlTypeToJsonSchema.baseCodeAction))
          case _ => Future.successful(Seq.empty)
        }
      }
    }
}

object RamlTypeToJsonSchema extends CodeActionFactory with RamlTypeToJsonSchemaKind {
  def apply(params: CodeActionRequestParams): CodeActionResponsePlugin =
    new RamlTypeToJsonSchema(params)
}
