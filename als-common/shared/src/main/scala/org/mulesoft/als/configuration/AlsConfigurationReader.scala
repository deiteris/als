package org.mulesoft.als.configuration

import org.mulesoft.lsp.configuration.FormatOptions

trait AlsConfigurationReader {
  // todo: add optional "experimental" feature? (to enable beta changes)
  def getFormatOptionForMime(mimeType: String): FormatOptions
  def supportsDocumentChanges: Boolean
  def getTemplateType: TemplateTypes.TemplateTypes
  def getShouldPrettyPrintSerialization: Boolean

  /**
    * Only set during Initialization through ProjectConfigurationClientCapabilities
    * in case of None, the project requests should be handled
    * in case of Some, there will be an attempt of reading the configuration through the file
    * @return
    */
  def getConfigurationType: Option[ProjectConfiguration]
}
