package analyzer.datasource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogFileFinderTest {
    private static final Path TEST_DIR = Path.of("src/test/resources");

    @BeforeAll
    static void setUp() throws IOException {
        Files.createDirectory(TEST_DIR);
        Files.createFile(TEST_DIR.resolve("log1.log"));
        Files.createFile(TEST_DIR.resolve("log2.log"));
        Files.createFile(TEST_DIR.resolve("log3.log"));
        Files.createFile(TEST_DIR.resolve("log.txt"));
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.walk(TEST_DIR)
            .sorted(Comparator.reverseOrder())
            .forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Test
    public void findLogFiles() {
        String path = "test/resources/*.log";
        LogFileFinder fileFinder = new LogFileFinder(path);
        List<Path> foundFiles;

        fileFinder.findLogFiles();
        foundFiles = fileFinder.files();

        assertEquals(3, foundFiles.size());
    }

    @Test
    public void findLogFilesTestWithSpecificFile() {
        String path = "test/resources/log.txt";
        LogFileFinder fileFinder = new LogFileFinder(path);
        List<Path> foundFiles;

        fileFinder.findLogFiles();
        foundFiles = fileFinder.files();

        assertEquals(1, foundFiles.size());
        assertTrue(foundFiles.getFirst().endsWith("log.txt"));
    }

    @Test
    public void findLogFilesTestWithNoFile() {
        String path = "test/resources/someFile.someFile";
        LogFileFinder fileFinder = new LogFileFinder(path);
        List<Path> foundFiles;

        fileFinder.findLogFiles();
        foundFiles = fileFinder.files();

        assertEquals(0, foundFiles.size());
    }
}
