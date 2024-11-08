package analyzer.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class NginxLogEntry {
    private static final Logger LOGGER = Logger.getLogger(NginxLogEntry.class.getName());
    private static final int MIN_STATUS_CODE = 100;
    private static final int MAX_STATUS_CODE = 599;
    private static final int MIN_IP_OCTET = 0;
    private static final int MAX_IP_OCTET = 255;
    private static final int IPV4_PARTS = 4;
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

    private final String clientIP;
    private final Optional<String> remoteUser;
    private final LocalDateTime localDateTime;
    private final String request;
    private final int statusCode;
    private final int bodyBytesSent;
    private final Optional<String> httpReferer;
    private final String httpUserAgent;
    private final Optional<String> httpMethod;
    private final Optional<String> resource;

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
            logWarning("The log entry does not include all parameters: " + logEntry);
            return Optional.empty();
        }

        try {
            return Optional.of(new Builder()
                .clientIp(validateIP(matcher.group(GROUP_CLIENT_IP)).orElseThrow())
                .remoteUser(validateRemoteUser(matcher.group(GROUP_REMOTE_USER)))
                .localDateTime(validateLocalTime(matcher.group(GROUP_LOCAL_TIME)).orElseThrow())
                .request(validateRequest(matcher.group(GROUP_REQUEST)).orElseThrow())
                .statusCode(validateStatusCode(matcher.group(GROUP_STATUS_CODE)).orElseThrow())
                .bodyBytesSent(validateBodyBytesSent(matcher.group(GROUP_BODY_BYTES_SENT)).orElseThrow())
                .httpReferer(validateHttpReferer(matcher.group(GROUP_HTTP_REFERER)))
                .httpUserAgent(validateUserAgent(matcher.group(GROUP_HTTP_USER_AGENT)).orElseThrow())
                .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<String> validateIP(String clientIP) {
        Pattern ipPattern = Pattern.compile("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|[a-fA-F0-9:]+");
        Matcher matcher = ipPattern.matcher(clientIP);
        String message = "Invalid request client IP address: " + clientIP;

        if (!matcher.find()) {
            logWarning(message);
            return Optional.empty();
        }

        String[] values = clientIP.split("\\.");
        if (values.length == IPV4_PARTS) {
            for (String value : values) {
                if (Integer.parseInt(value) < MIN_IP_OCTET || Integer.parseInt(value) > MAX_IP_OCTET) {
                    logWarning(message);
                    return Optional.empty();
                }
            }
        }

        return Optional.of(clientIP);
    }

    private static Optional<String> validateRemoteUser(String remoteUser) {
        if ("-".equals(remoteUser)) {
            return Optional.empty();
        } else {
            return Optional.of(remoteUser);
        }
    }

    private static Optional<LocalDateTime> validateLocalTime(String localDateTime) {
        try {
            return Optional.of(LocalDateTime.parse(localDateTime, NGINX_DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            logWarning("Invalid local time: " + localDateTime);
            return Optional.empty();
        }
    }

    private static Optional<String> validateRequest(String request) {
        if (!request.matches("(GET|POST|PUT|DELETE|HEAD|CONNECT|OPTIONS|TRACE|PATCH) [^ ]+ HTTP/\\d\\.\\d")) {
            logWarning("Invalid HTTP type request: " + request);
            return Optional.empty();
        }

        return Optional.of(request);
    }

    private static Optional<Integer> validateStatusCode(String statusCode) {
        String message = "Invalid HTTP server response code: " + statusCode;
        try {
            int code = Integer.parseInt(statusCode);
            if (code < MIN_STATUS_CODE || code > MAX_STATUS_CODE) {
                logWarning(message);
                return Optional.empty();
            }
            return Optional.of(code);
        } catch (NumberFormatException e) {
            logWarning(message);
            return Optional.empty();
        }
    }

    private static Optional<Integer> validateBodyBytesSent(String bodyBytesSent) {
        String message = "Invalid size in bytes of the server response: " + bodyBytesSent;
        try {
            int bytesSent = Integer.parseInt(bodyBytesSent);
            if (bytesSent < 0) {
                logWarning(message);
                return Optional.empty();
            }
            return Optional.of(bytesSent);
        } catch (NumberFormatException e) {
            logWarning(message);
            return Optional.empty();
        }
    }

    private static Optional<String> validateHttpReferer(String httpReferer) {
        if ("-".equals(httpReferer)) {
            return Optional.empty();
        } else {
            return Optional.of(httpReferer);
        }
    }

    private static Optional<String> validateUserAgent(String httpUserAgent) {
        if (httpUserAgent == null || httpUserAgent.isBlank()) {
            logWarning("Invalid HTTP user agent: " + httpUserAgent);
            return Optional.empty();
        }

        return Optional.of(httpUserAgent);
    }

    private static void logWarning(String message) {
        LOGGER.log(Level.WARNING, message);
    }

    public static class Builder {
        private String clientIP;
        private Optional<String> remoteUser;
        private LocalDateTime localDateTime;
        private String request;
        private int statusCode;
        private int bodyBytesSent;
        private Optional<String> httpReferer;
        private String httpUserAgent;
        private Optional<String> httpMethod;
        private Optional<String> resource;

        public Builder clientIp(String clientIP) {
            this.clientIP = clientIP;
            return this;
        }

        public Builder remoteUser(Optional<String> remoteUser) {
            this.remoteUser = remoteUser;
            return this;
        }

        public Builder localDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
            return this;
        }

        public Builder request(String request) {
            this.request = request;
            this.httpMethod = extractHttpMethod(request);
            this.resource = extractResource(request);
            return this;
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder bodyBytesSent(int bodyBytesSent) {
            this.bodyBytesSent = bodyBytesSent;
            return this;
        }

        public Builder httpReferer(Optional<String> httpReferer) {
            this.httpReferer = httpReferer;
            return this;
        }

        public Builder httpUserAgent(String httpUserAgent) {
            this.httpUserAgent = httpUserAgent;
            return this;
        }

        public NginxLogEntry build() {
            return new NginxLogEntry(this);
        }

        private Optional<String> extractHttpMethod(String request) {
            int spaceIndex = request.indexOf(' ');
            if (spaceIndex == -1) {
                NginxLogEntry.logWarning("Http method not found in request: " + request);
                return Optional.empty();
            }
            return Optional.of(request.substring(0, spaceIndex));
        }

        private Optional<String> extractResource(String request) {
            Pattern resourcePattern = Pattern.compile("\\S+\\s(\\S+)\\sHTTP/\\d\\.\\d");
            Matcher matcher = resourcePattern.matcher(request);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            } else {
                NginxLogEntry.logWarning("Resource not found in request: " + request);
                return Optional.empty();
            }
        }
    }
}
