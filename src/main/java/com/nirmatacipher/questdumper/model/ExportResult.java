package com.nirmatacipher.questdumper.model;

import java.nio.file.Path;

public record ExportResult(
        boolean success,
        String message,
        Path folderDumpPath,
        Path snbtPath,
        Path summaryPath,
        Path importReadyPath,
        Path infoPath
) {
    public static ExportResult success(String message, Path folderDumpPath, Path snbtPath, Path summaryPath, Path importReadyPath, Path infoPath) {
        return new ExportResult(true, message, folderDumpPath, snbtPath, summaryPath, importReadyPath, infoPath);
    }

    public static ExportResult failure(String message) {
        return new ExportResult(false, message, null, null, null, null, null);
    }

    public static ExportResult failure(String message, Path infoPath) {
        return new ExportResult(false, message, null, null, null, null, infoPath);
    }
}
