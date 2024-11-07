package analyzer.analyzer;

import analyzer.config.AnalyzerConfig;
import analyzer.model.NginxLogEntry;
import analyzer.output.OutputFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogFilterTest {
    private AnalyzerConfig analyzerConfig;
    private LogFilter logFilter;

    @BeforeEach
    public void setUp() {
        analyzerConfig = new AnalyzerConfig(
            Optional.of(LocalDateTime.parse("2015-05-17T00:00:00")),
            Optional.of(LocalDateTime.parse("2015-05-19T00:00:00")),
            OutputFormat.MARKDOWN, List.of(), Optional.empty(), Optional.empty(), Optional.empty()
        );

        logFilter = new LogFilter(analyzerConfig);
    }

    @Test
    public void isWithinRange() {
        String logEntryString1 =
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";
        String logEntryString2 =
            "93.180.71.3 - - [18/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        Optional<NginxLogEntry> logEntry1 = NginxLogEntry.parseNginxLogEntry(logEntryString1);
        Optional<NginxLogEntry> logEntry2 = NginxLogEntry.parseNginxLogEntry(logEntryString2);

        assertTrue(logEntry1.isPresent());
        assertTrue(logEntry2.isPresent());

        assertTrue(logFilter.isWithinRange(logEntry1.get().localDateTime()));
        assertTrue(logFilter.isWithinRange(logEntry2.get().localDateTime()));
    }

    @Test
    public void isWithinRangeWithInvalidDate() {
        String logEntryString1 =
            "93.180.71.3 - - [16/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";
        String logEntryString2 =
            "93.180.71.3 - - [19/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        Optional<NginxLogEntry> logEntry1 = NginxLogEntry.parseNginxLogEntry(logEntryString1);
        Optional<NginxLogEntry> logEntry2 = NginxLogEntry.parseNginxLogEntry(logEntryString2);

        assertTrue(logEntry1.isPresent());
        assertTrue(logEntry2.isPresent());

        assertFalse(logFilter.isWithinRange(logEntry1.get().localDateTime()));
        assertFalse(logFilter.isWithinRange(logEntry2.get().localDateTime()));
    }

    @Test
    public void isWithinRangeWithNoRange() {
        AnalyzerConfig configNoRange =
            new AnalyzerConfig(Optional.empty(), Optional.empty(), OutputFormat.MARKDOWN, List.of(), Optional.empty(),
                Optional.empty(), Optional.empty());
        logFilter = new LogFilter(configNoRange);
        String logEntryString1 =
            "93.180.71.3 - - [16/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";
        String logEntryString2 =
            "93.180.71.3 - - [19/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        Optional<NginxLogEntry> logEntry1 = NginxLogEntry.parseNginxLogEntry(logEntryString1);
        Optional<NginxLogEntry> logEntry2 = NginxLogEntry.parseNginxLogEntry(logEntryString2);

        assertTrue(logEntry1.isPresent());
        assertTrue(logEntry2.isPresent());

        assertTrue(logFilter.isWithinRange(logEntry1.get().localDateTime()));
        assertTrue(logFilter.isWithinRange(logEntry2.get().localDateTime()));
    }
}
