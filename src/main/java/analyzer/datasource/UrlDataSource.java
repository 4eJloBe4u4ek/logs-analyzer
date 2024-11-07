package analyzer.datasource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class UrlDataSource implements LogDataSource {
    private static final Logger logger = Logger.getLogger(UrlDataSource.class.getName());
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
            HttpResponse<Stream<String>> response = client.send(request, HttpResponse.BodyHandlers.ofLines());
            if (response.statusCode() != 200) {
                logger.log(Level.WARNING,
                    "Failed to fetch data from " + urlString + ". Response code: " + response.statusCode());
                return Stream.empty();
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            logger.log(Level.WARNING, "Error processing URL " + urlString + ": " + e.getMessage());
            return Stream.empty();
        }
    }
}
