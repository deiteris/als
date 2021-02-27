package org.mulesoft.als.suggestions.plugins.aml

import amf.plugins.document.vocabularies.model.domain.AnnotationMapping
import org.mulesoft.als.suggestions.RawSuggestion
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.interfaces.AMLCompletionPlugin
import org.mulesoft.amfintegration.AmfImplicits.AmfObjectImp

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
trait VendorExtensionsReferenceCompletionPlugin extends AMLCompletionPlugin {
  override def id: String = "VendorExtensionsReferenceCompletionPlugin"

  override def resolve(request: AmlCompletionRequest): Future[Seq[RawSuggestion]] = Future {
    if (request.yPartBranch.isKey && !request.yPartBranch.isInArray) {
      val mappings: List[String] = request.amfObject.metaURIs
        .flatMap(request.amfInstance.alsAmlPlugin.registry.extensionMappingForTerm)
        .map(_._1)
        .distinct
      mappings.map(annotationMask).map(n => RawSuggestion.forKey(n, "semantic-extension", mandatory = false))
    } else Nil
  }

  def annotationMask(name: String): String
}

object AMLVendorExtensionsReferenceCompletionPlugin extends VendorExtensionsReferenceCompletionPlugin {
  override def annotationMask(name: String): String = s"($name)"
}
