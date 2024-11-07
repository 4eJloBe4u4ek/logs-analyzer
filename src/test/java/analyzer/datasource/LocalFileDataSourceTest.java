package analyzer.datasource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocalFileDataSourceTest {
    private Path tempFile1;
    private Path tempFile2;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile1 = Files.createTempFile("test1", ".log");
        tempFile2 = Files.createTempFile("test2", ".log");

        Files.write(tempFile1, List.of("Log entry1"));
        Files.write(tempFile2, List.of("Log entry2"));
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempFile1);
        Files.deleteIfExists(tempFile2);
    }

    @Test
    public void getDataStreamWithValidData() {
        LocalFileDataSource fileDataSource = new LocalFileDataSource(List.of(tempFile1, tempFile2));

        assertTrue(fileDataSource.getDataStream().findAny().isPresent());
        assertEquals(2, fileDataSource.getDataStream().count());
    }

    @Test
    public void getDataStreamWithInvalidData() {
        LocalFileDataSource fileDataSource = new LocalFileDataSource(List.of());

        assertFalse(fileDataSource.getDataStream().findAny().isPresent());
    }
}
