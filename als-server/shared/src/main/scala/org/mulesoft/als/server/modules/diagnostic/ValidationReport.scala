package org.mulesoft.als.server.modules.diagnostic

import amf.ProfileName
import org.mulesoft.lsp.feature.diagnostic.PublishDiagnosticsParams

/**
  * Validation report.
  *
  * @param pointOfViewUri This is the "point of view" uri, actual reported unit paths are located
  *                       in the particular issues.
  * @param issues         Validation issues.
  *
  */
case class ValidationReport(pointOfViewUri: String, issues: Set[ValidationIssue], profile: ProfileName) {
  lazy val publishDiagnosticsParams: AlsPublishDiagnosticsParams = AlsPublishDiagnosticsParams(
    pointOfViewUri,
    issues.map(_.diagnostic).toSeq,
    profile
  )
}
