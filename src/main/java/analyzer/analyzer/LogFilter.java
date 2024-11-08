package analyzer.analyzer;

import analyzer.config.AnalyzerConfig;
import analyzer.model.NginxLogEntry;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class LogFilter {
    private final AnalyzerConfig analyzerConfig;

    public LogFilter(AnalyzerConfig analyzerConfig) {
        this.analyzerConfig = analyzerConfig;
    }

    public boolean isWithinRange(LocalDateTime dateTime) {
        boolean withinFromRange = analyzerConfig.from()
            .map(date -> !dateTime.isBefore(date))
            .orElse(Boolean.TRUE);

        boolean withinToRange = analyzerConfig.to()
            .map(date -> !dateTime.isAfter(date))
            .orElse(Boolean.TRUE);

        return withinFromRange && withinToRange;
    }

    public boolean matchByFieldAndValue(NginxLogEntry entry) {
        if (analyzerConfig.filterField().isEmpty() || analyzerConfig.filterValue().isEmpty()) {
            return true;
        }

        String actualFilterValue = switch (analyzerConfig.filterField().orElseThrow().toLowerCase()) {
            case "agent" -> entry.httpUserAgent();
            case "status" -> String.valueOf(entry.statusCode());
            case "resource" -> entry.resource().orElse(null);
            case "method" -> entry.httpMethod().orElse(null);
            case "ip" -> entry.clientIP();
            default -> null;
        };

        if (actualFilterValue == null) {
            return false;
        }

        String filterValue = analyzerConfig.filterValue().orElseThrow();
        String regex = filterValue.replace("*", ".*");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(actualFilterValue).matches();
    }
}
