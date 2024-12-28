package analyzer.datasource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@SuppressFBWarnings("OS_OPEN_STREAM")
public class LocalFileDataSource implements LogDataSource {
    private static final Logger LOGGER = Logger.getLogger(LocalFileDataSource.class.getName());
    private final List<Path> files;

    public LocalFileDataSource(List<Path> files) {
        this.files = files;
    }

    @Override
    public Stream<String> getDataStream() {
        if (files.isEmpty()) {
            LOGGER.log(Level.WARNING, "No files provided for reading");
            return Stream.empty();
        }

        return files.stream()
            .flatMap(file -> {
                try {
                    BufferedReader reader = Files.newBufferedReader(file);
                    return reader.lines().onClose(() -> {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            LOGGER.log(Level.WARNING, "Failed to close reader for file: " + file, e.getMessage());
                        }
                    });
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error processing file " + file + ": " + e.getMessage());
                    return Stream.empty();
                }
            });
    }
}
