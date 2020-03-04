package org.mulesoft.als.server.lsp4j.extension;

import org.eclipse.lsp4j.ServerCapabilities;

public class AlsServerCapabilities extends ServerCapabilities {

    private SerializationServerOptions serialization;

    private CleanDiagnosticTreeServerOptions cleanDiagnosticTree;

    private ConversionServerOptions conversion;

    public void setSerialization(SerializationServerOptions serialization) {
        this.serialization = serialization;
    }

    public void setCleanDiagnosticTree(CleanDiagnosticTreeServerOptions cleanDiagnosticTree) {
        this.cleanDiagnosticTree = cleanDiagnosticTree;
    }

    public void setConversion(ConversionServerOptions conversion) {
        this.conversion = conversion;
    }

    public SerializationServerOptions getSerialization() {
        return serialization;
    }

    public CleanDiagnosticTreeServerOptions getCleanDiagnosticTree() {
        return cleanDiagnosticTree;
    }

    public ConversionServerOptions getConversion() { return conversion; }
}