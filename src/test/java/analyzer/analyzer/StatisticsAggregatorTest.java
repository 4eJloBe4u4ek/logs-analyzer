package analyzer.analyzer;

import analyzer.model.NginxLogEntry;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatisticsAggregatorTest {
    private StatisticsAggregator statisticsAggregator;

    private static final String DEFAULT_IP = "93.180.71.3";
    private static final LocalDateTime DEFAULT_DATE = LocalDateTime.parse("2015-05-17T00:00:00");
    private static final String DEFAULT_USER_AGENT = "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)";
    private static final String DEFAULT_REQUEST = "GET /downloads/product_1 HTTP/1.1";
    private static final int STATUS_CODE = 200;
    private static final String EMPTY = "";

    @BeforeEach
    public void setUp() {
        statisticsAggregator = new StatisticsAggregator();
    }

    @Test
    public void getAverageResponseSize() {
        for (int i = 0; i < 20; i++) {
            statisticsAggregator.addLogEntry(createLogEntry(i));
        }
        assertEquals(10, statisticsAggregator.getAverageResponseSize());
    }

    @Test
    public void getPercentileResponseSize() {
        for (int i = 0; i < 20; i++) {
            statisticsAggregator.addLogEntry(createLogEntry(i));
        }
        assertEquals(18, statisticsAggregator.getPercentileResponseSize(95));
    }

    @Test
    public void getAverageResponseSizeWithNoData() {
        assertEquals(0, statisticsAggregator.getAverageResponseSize());
    }

    @Test
    public void getPercentileResponseSizeWithNoData() {
        assertEquals(0, statisticsAggregator.getPercentileResponseSize(95));
    }

    private NginxLogEntry createLogEntry(int bodyBytesSent) {
        return new NginxLogEntry.Builder()
            .clientIP(DEFAULT_IP)
            .remoteUser(EMPTY)
            .localDateTime(DEFAULT_DATE)
            .request(DEFAULT_REQUEST)
            .statusCode(STATUS_CODE)
            .bodyBytesSent(bodyBytesSent)
            .httpReferer(EMPTY)
            .httpUserAgent(DEFAULT_USER_AGENT)
            .build();
    }
}
