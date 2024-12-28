package analyzer.datasource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;

@SuppressFBWarnings("PATH_TRAVERSAL_IN")
public class LogFileFinder {
    private static final String GLOB_SPECIAL_CHARS = "*?[]{}";
    private static final String GLOB_REGEX = ".*[*?\\[\\]{}].*";
    private final String pathPattern;
    @Getter private List<Path> files;

    public LogFileFinder(String pathPattern) {
        this.pathPattern = FilenameUtils.normalize(pathPattern);
        this.files = new ArrayList<>();
    }

    public void findLogFiles() {
        try {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + normalizeGlob(pathPattern));
            Path startDir = determineStartDir(pathPattern);

            Files.walkFileTree(startDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (matcher.matches(startDir.relativize(file)) || matcher.matches(file.toAbsolutePath())) {
                        files.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path determineStartDir(String path) {
        String baseDir = containsGlob(path) ? getBaseDir(path) : path;
        Path normalizedPath = Paths.get(FilenameUtils.normalize(baseDir));

        if (!normalizedPath.isAbsolute()) {
            normalizedPath = Paths.get(".").resolve(normalizedPath);
        }

        return normalizedPath;
    }

    private String getBaseDir(String path) {
        int firstSpecialCharIndex = path.length();
        for (char specialChar : GLOB_SPECIAL_CHARS.toCharArray()) {
            int index = path.indexOf(specialChar);
            if (index != -1 && index < firstSpecialCharIndex) {
                firstSpecialCharIndex = index;
            }
        }

        if (firstSpecialCharIndex == path.length()) {
            return path;
        }

        return path.substring(0, firstSpecialCharIndex);
    }

    private boolean containsGlob(String path) {
        return path.matches(GLOB_REGEX);
    }

    private String normalizeGlob(String path) {
        return FilenameUtils.separatorsToUnix(path);
    }
}
