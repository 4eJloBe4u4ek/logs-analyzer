package analyzer.model;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NginxLogEntryTest {
    @Test
    public void parseNginxLogEntryWithValidData() {
        String logEntryString =
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        Optional<NginxLogEntry> logEntry = NginxLogEntry.parseNginxLogEntry(logEntryString);

        assertTrue(logEntry.isPresent());
        assertEquals("93.180.71.3", logEntry.orElseThrow().clientIP());
        assertEquals(LocalDateTime.parse("2015-05-17T08:05:32"), logEntry.orElseThrow().localDateTime());
        assertEquals("GET /downloads/product_1 HTTP/1.1", logEntry.orElseThrow().request());
        assertEquals(304, logEntry.orElseThrow().statusCode());
        assertEquals("Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)", logEntry.orElseThrow().httpUserAgent());
        assertEquals(Optional.empty(), logEntry.orElseThrow().remoteUser());
        assertEquals(Optional.empty(), logEntry.orElseThrow().httpReferer());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Invalid log format",
        "93.300.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
        "93.180.71.3 - - [17/May/2015:08:05:32] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"/downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 600 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 -10 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"\""
    })
    public void testInvalidLogEntries(String logEntryString) {
        Optional<NginxLogEntry> logEntry = NginxLogEntry.parseNginxLogEntry(logEntryString);
        assertFalse(logEntry.isPresent());
    }
}
