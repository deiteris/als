package org.mulesoft.als.server.feature.customvalidation

import org.mulesoft.lsp.feature.RequestType

object RegisterProfileNotificationType   extends RequestType[RegisterProfileParams, Unit]
object UnregisterProfileNotificationType extends RequestType[RegisterProfileParams, Unit]
