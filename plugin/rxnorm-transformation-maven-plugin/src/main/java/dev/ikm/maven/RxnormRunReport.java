package dev.ikm.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Collects per-run statistics about the RxNorm transformation and writes them to
 * {@code rxnorm-transformation-report.json} in the module's build directory.
 *
 * <p>The report records input identity (file name + sha256), the RxCUI resolution split
 * (annotation-corroborated vs completed-by-class-id), and any RxCUI annotation mismatches, so a
 * run can be traced without re-deriving anything from the store. The mojo runs once per JVM
 * (Maven forks per build), so static counters are safe; {@link #reset()} is called at the start
 * of every execution.
 */
public final class RxnormRunReport {

    public static final AtomicInteger classCount = new AtomicInteger();
    public static final AtomicInteger fullConceptCount = new AtomicInteger();
    public static final AtomicInteger labelOnlyConceptCount = new AtomicInteger();
    /** RxCUI identifier semantics whose value came from the OWL RxCUI annotation. */
    public static final AtomicInteger rxCuiFromAnnotation = new AtomicInteger();
    /** RxCUI identifier semantics synthesized with value = class id (no annotation present). */
    public static final AtomicInteger rxCuiCompletedByRule = new AtomicInteger();
    /** Classes whose OWL RxCUI annotation diverged from the class id (data-quality anomalies). */
    public static final AtomicInteger rxCuiMismatchCount = new AtomicInteger();
    public static final AtomicInteger errorCount = new AtomicInteger();

    private static final List<String> errors = new ArrayList<>();
    private static final List<String> rxCuiMismatches = new ArrayList<>();
    private static final int MAX_ERRORS = 100;

    private RxnormRunReport() {
    }

    public static synchronized void reset() {
        classCount.set(0);
        fullConceptCount.set(0);
        labelOnlyConceptCount.set(0);
        rxCuiFromAnnotation.set(0);
        rxCuiCompletedByRule.set(0);
        rxCuiMismatchCount.set(0);
        errorCount.set(0);
        errors.clear();
        rxCuiMismatches.clear();
    }

    /** Records an RxCUI annotation that diverges from the class id (capped sample list). */
    public static void recordRxCuiMismatch(String classId, String annotatedValue) {
        rxCuiMismatchCount.incrementAndGet();
        synchronized (rxCuiMismatches) {
            if (rxCuiMismatches.size() < MAX_ERRORS) {
                rxCuiMismatches.add(classId + " -> " + annotatedValue);
            }
        }
    }

    public static void recordError(String message) {
        errorCount.incrementAndGet();
        synchronized (errors) {
            if (errors.size() < MAX_ERRORS) {
                errors.add(message);
            }
        }
    }

    public static void writeJson(Path outputDir, String owlFileName, String owlSha256) throws IOException {
        StringBuilder sb = new StringBuilder(512);
        sb.append("{\n")
                .append("  \"generatedAt\": ").append(quote(String.valueOf(System.currentTimeMillis()))).append(",\n")
                .append("  \"sourceFile\": ").append(quote(owlFileName)).append(",\n")
                .append("  \"sourceSha256\": ").append(quote(owlSha256)).append(",\n")
                .append("  \"classesProcessed\": ").append(classCount.get()).append(",\n")
                .append("  \"fullConcepts\": ").append(fullConceptCount.get()).append(",\n")
                .append("  \"labelOnlyConcepts\": ").append(labelOnlyConceptCount.get()).append(",\n")
                .append("  \"rxCuiFromAnnotation\": ").append(rxCuiFromAnnotation.get()).append(",\n")
                .append("  \"rxCuiCompletedByRule\": ").append(rxCuiCompletedByRule.get()).append(",\n")
                .append("  \"rxCuiMismatchCount\": ").append(rxCuiMismatchCount.get()).append(",\n")
                .append("  \"rxCuiMismatchSamples\": [");
        synchronized (rxCuiMismatches) {
            for (int i = 0; i < rxCuiMismatches.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(quote(rxCuiMismatches.get(i)));
            }
        }
        sb.append("],\n")
                .append("  \"errorCount\": ").append(errorCount.get()).append(",\n")
                .append("  \"errors\": [");
        synchronized (errors) {
            for (int i = 0; i < errors.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(quote(errors.get(i)));
            }
        }
        sb.append("]\n}\n");
        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve("rxnorm-transformation-report.json"), sb.toString(), StandardCharsets.UTF_8);
    }

    /** sha256 hex of a file, or "" when unreadable (the report must never fail the build). */
    public static String sha256Hex(File file) {
        if (file == null || !file.isFile()) {
            return "";
        }
        try (InputStream in = Files.newInputStream(file.toPath())) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[1 << 16];
            int n;
            while ((n = in.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            return "";
        }
    }

    private static String quote(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder out = new StringBuilder(s.length() + 2);
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
