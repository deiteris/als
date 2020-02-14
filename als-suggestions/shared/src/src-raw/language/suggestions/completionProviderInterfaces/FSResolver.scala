package org.mulesoft.als.suggestions.completionProviderInterfaces

import org.mulesoft.als.suggestions.raml_1_parser.Raml1ParserIndex
import org.mulesoft.als.suggestions.completionProviderInterfaces.IEditorStateProvider;
import org.mulesoft.als.suggestions.completionProviderInterfaces.IASTProvider;
import org.mulesoft.als.suggestions.completionProviderInterfaces.IFSProvider;
import org.mulesoft.als.suggestions.completionProviderInterfaces.Suggestion;
import org.mulesoft.als.suggestions.completionProviderInterfaces.FSResolver;
import org.mulesoft.als.suggestions.completionProviderInterfaces.FSResolverExt;

trait FSResolver {
  def content(fullPath: String): String
  def contentAsync(fullPath: String): Promise[String]
}