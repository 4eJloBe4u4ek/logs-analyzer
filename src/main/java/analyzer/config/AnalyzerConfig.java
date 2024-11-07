package analyzer.config;

import analyzer.output.OutputFormat;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class AnalyzerConfig {
    private static final Map<Integer, String> STATUS_CODES = Map.<Integer, String>ofEntries(
        Map.entry(100, "Continue"),
        Map.entry(101, "Switching Protocols"),
        Map.entry(102, "Processing"),
        Map.entry(103, "Early Hints"),
        Map.entry(200, "OK"),
        Map.entry(201, "Created"),
        Map.entry(202, "Accepted"),
        Map.entry(203, "Non-Authoritative Information"),
        Map.entry(204, "No Content"),
        Map.entry(205, "Reset Content"),
        Map.entry(206, "Partial Content"),
        Map.entry(207, "Multi-Status"),
        Map.entry(208, "Already Reported"),
        Map.entry(226, "IM Used"),
        Map.entry(300, "Multiple Choices"),
        Map.entry(301, "Moved Permanently"),
        Map.entry(302, "Found"),
        Map.entry(303, "See Other"),
        Map.entry(304, "Not Modified"),
        Map.entry(305, "Use Proxy"),
        Map.entry(306, "Unused"),
        Map.entry(307, "Temporary Redirect"),
        Map.entry(308, "Permanent Redirect"),
        Map.entry(400, "Bad Request"),
        Map.entry(401, "Unauthorized"),
        Map.entry(402, "Payment Required"),
        Map.entry(403, "Forbidden"),
        Map.entry(404, "Not Found"),
        Map.entry(405, "Method Not Allowed"),
        Map.entry(406, "Not Acceptable"),
        Map.entry(407, "Proxy Authentication Required"),
        Map.entry(408, "Request Timeout"),
        Map.entry(409, "Conflict"),
        Map.entry(410, "Gone"),
        Map.entry(411, "Length Required"),
        Map.entry(412, "Precondition Failed"),
        Map.entry(413, "Content Too Large"),
        Map.entry(414, "URI Too Long"),
        Map.entry(415, "Unsupported Media Type"),
        Map.entry(416, "Range Not Satisfiable"),
        Map.entry(417, "Expectation Failed"),
        Map.entry(418, "I'm a teapot"),
        Map.entry(421, "Misdirected Request"),
        Map.entry(422, "Unprocessable Content"),
        Map.entry(423, "Locked"),
        Map.entry(424, "Failed Dependency"),
        Map.entry(425, "Too Early"),
        Map.entry(426, "Upgrade Required"),
        Map.entry(428, "Precondition Required"),
        Map.entry(429, "Too Many Requests"),
        Map.entry(431, "Request Header Fields Too Large"),
        Map.entry(451, "Unavailable For Legal Reasons"),
        Map.entry(500, "Internal Server Error"),
        Map.entry(501, "Not Implemented"),
        Map.entry(502, "Bad Gateway"),
        Map.entry(503, "Service Unavailable"),
        Map.entry(504, "Gateway Timeout"),
        Map.entry(505, "HTTP Version Not Supported"),
        Map.entry(506, "Variant Also Negotiates"),
        Map.entry(507, "Insufficient Storage"),
        Map.entry(508, "Loop Detected"),
        Map.entry(510, "Not Extended"),
        Map.entry(511, "Network Authentication Required"));

    private static final Path MARKDOWN_PATH = Path.of("src/main/resources/statistics.md");
    private static final Path ADOC_PATH = Path.of("src/main/resources/statistics.adoc");

    private final Optional<LocalDateTime> from;
    private final Optional<LocalDateTime> to;
    private final OutputFormat format;
    private final List<Path> files;
    private final Optional<String> urlString;
    private final Optional<String> filterField;
    private final Optional<String> filterValue;

    public AnalyzerConfig(
        Optional<LocalDateTime> from,
        Optional<LocalDateTime> to,
        OutputFormat format,
        List<Path> files,
        Optional<String> urlString,
        Optional<String> filterField,
        Optional<String> filterValue
    ) {
        this.from = from;
        this.to = to;
        this.format = format;
        this.files = files;
        this.urlString = urlString;
        this.filterField = filterField;
        this.filterValue = filterValue;
    }

    public Path getOutputPath() {
        return switch (format) {
            case MARKDOWN -> MARKDOWN_PATH;
            case ADOC -> ADOC_PATH;
            default -> MARKDOWN_PATH;
        };
    }

    public String getStatusDescription(int code) {
        return STATUS_CODES.getOrDefault(code, "Unknown Status");
    }

    public String getDataSourceAsString() {
        if (urlString.isPresent()) {
            String url = urlString.get();
            int maxLength = 30;
            if (url.length() > maxLength) {
                return "URL: `" + url.substring(0, maxLength / 2) + "..." +
                    url.substring(url.length() - maxLength / 2) + '`';
            } else {
                return "URL: " + url;
            }
        } else if (!files().isEmpty()) {
            return files.stream()
                .map(file -> '`' + file.getFileName().toString() + '`')
                .collect(Collectors.joining(", "));
        }

        return "-";
    }
}
