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
    private static final Logger logger = Logger.getLogger(NginxLogEntry.class.getName());
    private static final DateTimeFormatter NGINX_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);
    private static final Pattern LOG_PATTERN = Pattern.compile(
        "^"
            + "(\\S+)\\s+"               // Client IP
            + "(-)\\s+"                  // Hyphen (ignored)
            + "(\\S+)\\s+"               // Remote user
            + "\\[(.*?)]\\s+"            // Local time
            + "\"(.*?)\"\\s+"            // Request
            + "(\\d{3})\\s+"             // Status code
            + "(\\d+)\\s+"               // Body bytes sent
            + "\"(.*?)\"\\s+"            // HTTP Referer
            + "\"(.*?)\"$",              // HTTP User Agent
        Pattern.COMMENTS
    );

    private final String clientIP;
    private final Optional<String> remoteUser;
    private final LocalDateTime localDateTime;
    private final String request;
    private final int statusCode;
    private final int bodyBytesSent;
    private final Optional<String> httpReferer;
    private final String httpUserAgent;

    public NginxLogEntry(
        String clientIp,
        Optional<String> remoteUser,
        LocalDateTime localDateTime,
        String request,
        int statusCode,
        int bodyBytesSent,
        Optional<String> httpReferer,
        String httpUserAgent
    ) {
        this.clientIP = clientIp;
        this.remoteUser = remoteUser;
        this.localDateTime = localDateTime;
        this.request = request;
        this.statusCode = statusCode;
        this.bodyBytesSent = bodyBytesSent;
        this.httpReferer = httpReferer;
        this.httpUserAgent = httpUserAgent;
    }

    public static Optional<NginxLogEntry> parseNginxLogEntry(String logEntry) {
        Matcher matcher = LOG_PATTERN.matcher(logEntry);

        if (!matcher.matches()) {
            logWarning("The log entry does not include all parameters: " + logEntry);
            return Optional.empty();
        }

        try {
            return Optional.of(new NginxLogEntry(
                validateIP(matcher.group(1)).orElseThrow(),
                validateRemoteUser(matcher.group(3)),
                validateLocalTime(matcher.group(4)).orElseThrow(),
                validateRequest(matcher.group(5)).orElseThrow(),
                validateStatusCode(matcher.group(6)).orElseThrow(),
                validateBodyBytesSent(matcher.group(7)).orElseThrow(),
                validateHttpReferer(matcher.group(8)),
                validateUserAgent(matcher.group(9)).orElseThrow()
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> extractResource() {
        Pattern resourcePattern = Pattern.compile("\\S+\\s(\\S+)\\sHTTP/\\d\\.\\d");
        Matcher matcher = resourcePattern.matcher(request);

        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        } else {
            logWarning("Resource not found in request: " + request);
            return Optional.empty();
        }
    }

    public String extractHttpMethod() {
        return request.split(" ")[0];
    }

    private static Optional<String> validateIP(String clientIP) {
        Pattern ipPattern = Pattern.compile("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|[a-fA-F0-9:]+");
        Matcher matcher = ipPattern.matcher(clientIP);

        if (!matcher.find()) {
            logWarning("Invalid request client IP address: " + clientIP);
            return Optional.empty();
        }

        String[] values = clientIP.split("\\.");
        if (values.length == 4) {
            for (String value : values) {
                if (Integer.parseInt(value) < 0 || Integer.parseInt(value) > 255) {
                    logWarning("Invalid request client IP address: " + clientIP);
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
        try {
            if (Integer.parseInt(statusCode) < 100 || Integer.parseInt(statusCode) > 599) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            logWarning("Invalid HTTP server response code: " + statusCode);
            return Optional.empty();
        }

        return Optional.of(Integer.parseInt(statusCode));
    }

    private static Optional<Integer> validateBodyBytesSent(String bodyBytesSent) {
        try {
            if (Integer.parseInt(bodyBytesSent) < 0) {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            logWarning("Invalid size in bytes of the server response: " + bodyBytesSent);
            return Optional.empty();
        }

        return Optional.of(Integer.parseInt(bodyBytesSent));
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
        logger.log(Level.WARNING, message);
    }
}
