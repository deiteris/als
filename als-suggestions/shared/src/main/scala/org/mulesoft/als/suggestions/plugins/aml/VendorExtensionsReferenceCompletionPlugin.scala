package org.mulesoft.als.suggestions.plugins.aml

import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.amfintegration.AmfImplicits.AmfObjectImp

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
trait VendorExtensionsReferenceCompletionPlugin extends AMLCompletionPlugin {
  override def id: String = "VendorExtensionsReferenceCompletionPlugin"

  override def resolve(request: AmlCompletionRequest): Future[Seq[RawSuggestion]] = Future {
    Seq.empty
  }

  def annotationMask(name: String): String
}

object AMLVendorExtensionsReferenceCompletionPlugin extends VendorExtensionsReferenceCompletionPlugin {
  override def annotationMask(name: String): String = s"($name)"
}
