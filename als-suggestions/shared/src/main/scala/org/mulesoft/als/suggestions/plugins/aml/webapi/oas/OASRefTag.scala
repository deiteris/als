package org.mulesoft.als.suggestions.plugins.aml.webapi.oas

import amf.plugins.domain.webapi.models.{EndPoint, Request}
import org.mulesoft.als.common.YPartBranch
import org.mulesoft.als.suggestions.aml.AmlCompletionRequest
import org.mulesoft.als.suggestions.plugins.aml.AMLRefTagCompletionPlugin

object OASRefTag extends AMLRefTagCompletionPlugin {

  override protected def isObjectDeclarable(params: AmlCompletionRequest): Boolean =
    super.isObjectDeclarable(params) || ((params.amfObject
      .isInstanceOf[EndPoint] || params.amfObject.isInstanceOf[Request]) && !params.yPartBranch.isKeyDescendanceOf(
      "parameters"))

  override def isExceptionCase(branch: YPartBranch): Boolean =
    branch.isKeyDescendanceOf("required") ||
      ((branch.isValue || (branch.isInArray && branch.stringValue == "x")) && branch.parentEntryIs("required"))
}