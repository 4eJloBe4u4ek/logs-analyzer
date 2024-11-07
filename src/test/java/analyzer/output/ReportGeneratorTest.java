package analyzer.output;

import analyzer.analyzer.StatisticsAggregator;
import analyzer.config.AnalyzerConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReportGeneratorTest {
    private AnalyzerConfig mockConfig;
    private StatisticsAggregator mockAggregator;
    private ReportGenerator reportGenerator;
    private Path tempFile;

    @BeforeEach
    public void setup() {
        mockConfig = mock(AnalyzerConfig.class);
        mockAggregator = mock(StatisticsAggregator.class);
        reportGenerator = new ReportGenerator(mockConfig, mockAggregator);

        when(mockConfig.getDataSourceAsString()).thenReturn("Test DataSource");
        when(mockConfig.from()).thenReturn(Optional.of(LocalDateTime.parse("2015-05-17T00:00")));
        when(mockConfig.to()).thenReturn(Optional.of(LocalDateTime.parse("2015-05-18T00:00")));

        when(mockAggregator.totalRequests()).thenReturn(1000);
        when(mockAggregator.getAverageResponseSize()).thenReturn(500);
        when(mockAggregator.getPercentileResponseSize(95)).thenReturn(1000);
        when(mockAggregator.getTopResources(10)).thenReturn(Map.of("/home", 500, "/login", 500));
        when(mockAggregator.getTopStatusCodes(10)).thenReturn(Map.of(200, 800, 404, 200));
        when(mockAggregator.getTopHttpMethods(10)).thenReturn(Map.of("GET", 900, "POST", 100));
        when(mockAggregator.getTopIpAddresses(10)).thenReturn(Map.of("192.168.1.1", 400, "10.0.0.1", 600));
        when(mockConfig.getStatusDescription(200)).thenReturn("OK");
        when(mockConfig.getStatusDescription(404)).thenReturn("Not Found");
    }

    @Test
    public void generateStatisticsReportMarkdownFormat() throws IOException {
        tempFile = Files.createTempFile("report", ".md");
        when(mockConfig.format()).thenReturn(OutputFormat.MARKDOWN);
        when(mockConfig.getOutputPath()).thenReturn(tempFile);

        reportGenerator.saveStatisticsToFile(mockConfig.format());
        List<String> lines = Files.readAllLines(tempFile);

        assertTrue(lines.contains("#### Общая информация"));
        assertTrue(lines.contains("#### Запрашиваемые ресурсы"));
        assertTrue(lines.contains("#### Коды ответа"));
        assertTrue(lines.contains("#### Http-методы"));
        assertTrue(lines.contains("#### Топ активных IP-адресов"));

        assertTrue(lines.contains("| Метрика               | Значение         |"));
        assertTrue(lines.contains("|:---------------------:|:----------------:|"));
        assertTrue(lines.contains("| Источник данных       | Test DataSource  |"));
        assertTrue(lines.contains("| Начальная дата        | 2015-05-17T00:00 |"));
        assertTrue(lines.contains("| Конечная дата         | 2015-05-18T00:00 |"));
        assertTrue(lines.contains("| Количество запросов   | 1_000            |"));
        assertTrue(lines.contains("| Средний размер ответа | 500b             |"));
        assertTrue(lines.contains("| 95p размера ответа    | 1_000b           |"));

        assertTrue(lines.contains("| Ресурс   | Количество |"));
        assertTrue(lines.contains("|:--------:|:----------:|"));
        assertTrue(lines.contains("| `/home`  | 500        |"));
        assertTrue(lines.contains("| `/login` | 500        |"));

        assertTrue(lines.contains("| Код | Имя       | Количество |"));
        assertTrue(lines.contains("|:---:|:---------:|:----------:|"));
        assertTrue(lines.contains("| 200 | OK        | 800        |"));
        assertTrue(lines.contains("| 404 | Not Found | 200        |"));

        assertTrue(lines.contains("| Метод  | Количество |"));
        assertTrue(lines.contains("|:------:|:----------:|"));
        assertTrue(lines.contains("| `GET`  | 900        |"));
        assertTrue(lines.contains("| `POST` | 100        |"));

        assertTrue(lines.contains("| IP-адрес    | Количество |"));
        assertTrue(lines.contains("|:-----------:|:----------:|"));
        assertTrue(lines.contains("| 192.168.1.1 | 400        |"));
        assertTrue(lines.contains("| 10.0.0.1    | 600        |"));

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testGenerateStatisticsReportAdocFormat() throws IOException {
        tempFile = Files.createTempFile("report", ".adoc");
        when(mockConfig.format()).thenReturn(OutputFormat.ADOC);
        when(mockConfig.getOutputPath()).thenReturn(tempFile);

        reportGenerator.saveStatisticsToFile(mockConfig.format());
        List<String> lines = Files.readAllLines(tempFile);

        assertTrue(lines.contains("==== Общая информация"));
        assertTrue(lines.contains("==== Запрашиваемые ресурсы"));
        assertTrue(lines.contains("==== Коды ответа"));
        assertTrue(lines.contains("==== Http-методы"));
        assertTrue(lines.contains("==== Топ активных IP-адресов"));
        assertTrue(lines.contains("|==="));

        assertTrue(lines.contains("| Метрика               | Значение         "));
        assertTrue(lines.contains("| Источник данных       | Test DataSource  "));
        assertTrue(lines.contains("| Начальная дата        | 2015-05-17T00:00 "));
        assertTrue(lines.contains("| Конечная дата         | 2015-05-18T00:00 "));
        assertTrue(lines.contains("| Количество запросов   | 1_000            "));
        assertTrue(lines.contains("| Средний размер ответа | 500b             "));
        assertTrue(lines.contains("| 95p размера ответа    | 1_000b           "));

        assertTrue(lines.contains("| Ресурс   | Количество "));
        assertTrue(lines.contains("| `/home`  | 500        "));
        assertTrue(lines.contains("| `/login` | 500        "));

        assertTrue(lines.contains("| Код | Имя       | Количество "));
        assertTrue(lines.contains("| 200 | OK        | 800        "));
        assertTrue(lines.contains("| 404 | Not Found | 200        "));

        assertTrue(lines.contains("| Метод  | Количество "));
        assertTrue(lines.contains("| `GET`  | 900        "));
        assertTrue(lines.contains("| `POST` | 100        "));

        assertTrue(lines.contains("| IP-адрес    | Количество "));
        assertTrue(lines.contains("| 192.168.1.1 | 400        "));
        assertTrue(lines.contains("| 10.0.0.1    | 600        "));

        Files.deleteIfExists(tempFile);
    }
}
