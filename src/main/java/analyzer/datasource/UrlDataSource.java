package analyzer.datasource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@SuppressFBWarnings("OS_OPEN_STREAM")
public class UrlDataSource implements LogDataSource {
    private static final Logger LOGGER = Logger.getLogger(UrlDataSource.class.getName());
    private static final int OK = 200;
    private final String urlString;

    public UrlDataSource(String urlString) {
        this.urlString = urlString;
    }

    @Override
    public Stream<String> getDataStream() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlString))
            .build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != OK) {
                LOGGER.log(Level.WARNING,
                    "Failed to fetch data from " + urlString + ". Response code: " + response.statusCode());
                return Stream.empty();
            }

            InputStream inputStream = response.body();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            return reader.lines().onClose(() -> {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to close reader for URL: " + urlString, e.getMessage());
                }
            });
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "Error processing URL " + urlString + ": " + e.getMessage());
            return Stream.empty();
        }
    }
}
