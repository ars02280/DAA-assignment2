package benchmark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Writes benchmark results as CSV. */
public final class CsvWriter {

    private CsvWriter() {
    }

    /** Writes {@code results.csv}-style output (header + one line per {@link Result}). */
    public static void write(Path file, List<Result> results) {
        List<String> lines = new ArrayList<>();
        lines.add(Result.CSV_HEADER);
        for (Result r : results) {
            lines.add(r.toCsvRow());
        }
        writeLines(file, lines);
    }

    static void writeLines(Path file, List<String> lines) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }
}
