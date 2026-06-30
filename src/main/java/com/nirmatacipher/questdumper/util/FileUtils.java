package com.nirmatacipher.questdumper.util;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

public final class FileUtils {
    private FileUtils() {
    }

    public static void ensureDirectory(Path path) throws IOException {
        Files.createDirectories(path);
    }

    public static void copyRecursive(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(source)) {
            for (Path src : stream.toList()) {
                Path dest = target.resolve(source.relativize(src).toString());
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public static boolean openDirectory(Path path) {
        try {
            Files.createDirectories(path);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(path.toFile());
                return true;
            }
        } catch (Throwable ignored) {
        }

        try {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", path.toAbsolutePath().toString()).start();
                return true;
            }
            if (os.contains("mac")) {
                new ProcessBuilder("open", path.toAbsolutePath().toString()).start();
                return true;
            }
            new ProcessBuilder("xdg-open", path.toAbsolutePath().toString()).start();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static int cleanGeneratedExports(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return 0;
        }

        int removed = 0;
        try (Stream<Path> stream = Files.list(root)) {
            for (Path path : stream.toList()) {
                String name = path.getFileName().toString();
                if (isGeneratedExportName(name)) {
                    deleteRecursive(path);
                    removed++;
                }
            }
        }
        return removed;
    }

    private static boolean isGeneratedExportName(String name) {
        return name.startsWith("ftbquests-full-")
                || name.startsWith("import-ready-")
                || name.startsWith("ftbquests-raw-") && name.endsWith(".snbt")
                || name.startsWith("questfile-summary-") && name.endsWith(".json")
                || name.startsWith("README-") && name.endsWith(".txt");
    }

    public static void deleteRecursive(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(path)) {
            for (Path item : stream.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(item);
            }
        }
    }
}
