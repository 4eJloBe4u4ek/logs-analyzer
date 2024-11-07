package analyzer.datasource;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class LogFileFinder {
    private static final Path START_DIR = Path.of("src");
    private final String globPattern;
    @Getter private List<Path> files;

    public LogFileFinder(String path) {
        this.globPattern = "glob:" + path;
        this.files = new ArrayList<>();
    }

    public void findLogFiles() {
        try {
            Files.walkFileTree(START_DIR, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);

                    if (matcher.matches(START_DIR.relativize(file))) {
                        files.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
