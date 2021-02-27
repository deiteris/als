package org.mulesoft.als.suggestions.plugins.aml.webapi

import org.mulesoft.als.suggestions.plugins.aml.VendorExtensionsReferenceCompletionPlugin

object OasLikeVendorExtensionsReferenceCompletionPlugin extends VendorExtensionsReferenceCompletionPlugin {
  override def annotationMask(name: String): String = s"x-$name"
}
