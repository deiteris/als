package org.mulesoft.als.configuration

case class AlsConfiguration(private var formattingOptions: Map[String, AlsFormattingOptions] = Map())
    extends AlsConfigurationReader {

  private var enableUpdateFormatOptions = true;

  def getFormattingOptions(mimeType: String): AlsFormatOptions =
    formattingOptions.getOrElse(mimeType, DefaultAlsFormattingOptions)

  def updateFormattingOptions(options: Map[String, AlsFormattingOptions]): Unit =
    if (enableUpdateFormatOptions)
      options.foreach(pair => {
        this.formattingOptions + pair
      })

  def setUpdateFormatOptions(enableUpdateFormatOptions: Boolean): Unit = {
    this.enableUpdateFormatOptions = enableUpdateFormatOptions;
  }

  def updateFormatOptionsIsEnabled(): Boolean = enableUpdateFormatOptions;

}