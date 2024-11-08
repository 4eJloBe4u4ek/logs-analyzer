package analyzer.datasource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

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
                    return Files.lines(file);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error processing file " + file + ": " + e.getMessage());
                    return Stream.empty();
                }
            });
    }
}
