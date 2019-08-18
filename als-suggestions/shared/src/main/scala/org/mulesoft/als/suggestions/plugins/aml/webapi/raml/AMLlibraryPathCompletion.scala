package org.mulesoft.als.suggestions.plugins.aml.webapi.raml

import amf.core.model.document.BaseUnit
import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.als.suggestions.plugins.aml.AMLPathCompletionPlugin

import scala.concurrent.Future

object AMLlibraryPathCompletion extends AMLCompletionPlugin {
  override def id: String = "AMLlibraryPathCompletion"

  override def resolve(request: AmlCompletionRequest): Future[Seq[RawSuggestion]] = {

    if ((request.amfObject
          .isInstanceOf[BaseUnit] || isEncodes(request.amfObject, request.actualDialect)) && request.yPartBranch
          .isValueDescendanceOf("uses")) {
      AMLPathCompletionPlugin.resolveInclusion(request.baseUnit.location().getOrElse(""),
                                               request.platform,
                                               request.prefix,
                                               request.directoryResolver)
    } else emptySuggestion
  }
}
