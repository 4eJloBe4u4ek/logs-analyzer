package analyzer.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
public class NginxLogEntry {
    private static final Logger LOGGER = Logger.getLogger(NginxLogEntry.class.getName());

    private static final int GROUP_CLIENT_IP = 1;
    private static final int GROUP_REMOTE_USER = 3;
    private static final int GROUP_LOCAL_TIME = 4;
    private static final int GROUP_REQUEST = 5;
    private static final int GROUP_STATUS_CODE = 6;
    private static final int GROUP_BODY_BYTES_SENT = 7;
    private static final int GROUP_HTTP_REFERER = 8;
    private static final int GROUP_HTTP_USER_AGENT = 9;

    private static final String CLIENT_IP = "(\\S+)\\s+";
    private static final String HYPHEN = "(-)\\s+";
    private static final String REMOTE_USER = CLIENT_IP;
    private static final String LOCAL_TIME = "\\[(.*?)]\\s+";
    private static final String REQUEST = "\"(.*?)\"\\s+";
    private static final String STATUS_CODE = "(\\d{3})\\s+";
    private static final String BODY_BYTES_SENT = "(\\d+)\\s+";
    private static final String HTTP_REFERER = REQUEST;
    private static final String HTTP_USER_AGENT = "\"(.*?)\"";

    private static final DateTimeFormatter NGINX_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);
    private static final Pattern LOG_PATTERN = Pattern.compile(
        "^"
            + CLIENT_IP
            + HYPHEN
            + REMOTE_USER
            + LOCAL_TIME
            + REQUEST
            + STATUS_CODE
            + BODY_BYTES_SENT
            + HTTP_REFERER
            + HTTP_USER_AGENT
            + "$");
    private static final Pattern RESOURCE_PATTERN = Pattern.compile("\\S+\\s(\\S+)\\sHTTP/\\d\\.\\d");

    private final String clientIP;
    private final String remoteUser;
    private final LocalDateTime localDateTime;
    private final String request;
    private final int statusCode;
    private final int bodyBytesSent;
    private final String httpReferer;
    private final String httpUserAgent;
    private final String httpMethod;
    private final String resource;

    private NginxLogEntry(Builder builder) {
        this.clientIP = builder.clientIP;
        this.remoteUser = builder.remoteUser;
        this.localDateTime = builder.localDateTime;
        this.request = builder.request;
        this.statusCode = builder.statusCode;
        this.bodyBytesSent = builder.bodyBytesSent;
        this.httpReferer = builder.httpReferer;
        this.httpUserAgent = builder.httpUserAgent;
        this.httpMethod = builder.httpMethod;
        this.resource = builder.resource;
    }

    public static Optional<NginxLogEntry> parseNginxLogEntry(String logEntry) {
        Matcher matcher = LOG_PATTERN.matcher(logEntry);

        if (!matcher.matches()) {
            logWarning("Log entry does not match the expected format: " + logEntry);
            return Optional.empty();
        }

        try {
            return Optional.of(new Builder()
                .clientIP(matcher.group(GROUP_CLIENT_IP))
                .remoteUser(matcher.group(GROUP_REMOTE_USER))
                .localDateTime(LocalDateTime.parse(matcher.group(GROUP_LOCAL_TIME), NGINX_DATE_FORMATTER))
                .request(matcher.group(GROUP_REQUEST))
                .statusCode(Integer.parseInt(matcher.group(GROUP_STATUS_CODE)))
                .bodyBytesSent(Integer.parseInt(matcher.group(GROUP_BODY_BYTES_SENT)))
                .httpReferer(matcher.group(GROUP_HTTP_REFERER))
                .httpUserAgent(matcher.group(GROUP_HTTP_USER_AGENT))
                .build());
        } catch (Exception e) {
            logWarning("Error parsing log entry: " + logEntry);
            return Optional.empty();
        }
    }

    private static void logWarning(String message) {
        LOGGER.log(Level.WARNING, message);
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private String clientIP;
        private String remoteUser;
        private LocalDateTime localDateTime;
        private String request;
        private int statusCode;
        private int bodyBytesSent;
        private String httpReferer;
        private String httpUserAgent;
        private String httpMethod;
        private String resource;

        public Builder request(String request) {
            this.request = request;
            this.httpMethod = extractHttpMethod(request);
            this.resource = extractResource(request);

            return this;
        }

        public NginxLogEntry build() {
            return new NginxLogEntry(this);
        }

        private String extractHttpMethod(String request) {
            return request.split(" ")[0];
        }

        private String extractResource(String request) {
            Matcher matcher = RESOURCE_PATTERN.matcher(request);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                logWarning("Resource not found in request: " + request);
                return "Unknown Resource";
            }
        }
    }
}
