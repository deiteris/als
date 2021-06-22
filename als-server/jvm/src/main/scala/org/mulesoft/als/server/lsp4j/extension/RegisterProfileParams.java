package org.mulesoft.als.server.lsp4j.extension;

import org.eclipse.lsp4j.TextDocumentIdentifier;

public class RegisterProfileParams {
    private final TextDocumentIdentifier textDocument;

    public RegisterProfileParams(TextDocumentIdentifier textDocument) {
        this.textDocument = textDocument;
    }

    public TextDocumentIdentifier getTextDocument() {
        return textDocument;
    }
}
