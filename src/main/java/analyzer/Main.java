package analyzer;

import analyzer.analyzer.NginxLogAnalyzer;
import analyzer.config.AnalyzerConfig;
import analyzer.config.CliParams;
import analyzer.datasource.LocalFileDataSource;
import analyzer.datasource.LogDataSource;
import analyzer.datasource.LogFileFinder;
import analyzer.datasource.UrlDataSource;
import analyzer.output.OutputFormat;
import analyzer.output.ReportGenerator;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String MARKDOWN_FORMAT = "markdown";
    private static final String ADOC_FORMAT = "adoc";

    public static void main(String[] args) throws IOException {
        CliParams params = new CliParams();
        JCommander.newBuilder()
            .addObject(params)
            .build()
            .parse(args);

        AnalyzerConfig analyzerConfig = setupAnalyzerConfig(params);
        LogDataSource dataSource = setupLogDataSource(analyzerConfig);

        NginxLogAnalyzer analyzer = new NginxLogAnalyzer(analyzerConfig, dataSource);
        analyzer.analyze();

        ReportGenerator reportGenerator =
            new ReportGenerator(analyzer.analyzerConfig(), analyzer.statisticsAggregator());
        reportGenerator.saveStatisticsToFile(analyzerConfig.format());
        reportGenerator.printStatisticsToConsole(analyzerConfig.format());
    }

    private static AnalyzerConfig setupAnalyzerConfig(CliParams params) {
        Optional<String> filterField = params.filterField();
        Optional<String> filterValue = params.filterValue();

        Optional<LocalDateTime> from = parseDate(params.from());
        Optional<LocalDateTime> to = parseDate(params.to());

        OutputFormat format = parseOutputFormat(params.format().orElse(MARKDOWN_FORMAT));

        List<Path> files = new ArrayList<>();
        Optional<String> urlString = Optional.empty();
        if (params.path().startsWith("http://") || params.path().startsWith("https://")) {
            urlString = params.path().describeConstable();
        } else {
            LogFileFinder fileFinder = new LogFileFinder(params.path());
            fileFinder.findLogFiles();
            files = fileFinder.files();
        }

        return new AnalyzerConfig(from, to, format, files, urlString, filterField, filterValue);
    }

    private static LogDataSource setupLogDataSource(AnalyzerConfig analyzerConfig) {
        return analyzerConfig.urlString().isPresent()
            ? new UrlDataSource(analyzerConfig.urlString().orElseThrow())
            : new LocalFileDataSource(analyzerConfig.files());
    }

    private static Optional<LocalDateTime> parseDate(Optional<String> dateString) {
        if (dateString.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDateTime.parse(dateString.orElseThrow(), DateTimeFormatter.ISO_DATE_TIME));
        } catch (DateTimeParseException firstException) {
            try {
                return Optional.of(
                    LocalDate.parse(dateString.orElseThrow(), DateTimeFormatter.ISO_DATE).atStartOfDay());
            } catch (DateTimeParseException secondException) {
                LOGGER.log(Level.WARNING, "Invalid date format: " + dateString);
                return Optional.empty();
            }
        }
    }

    private static OutputFormat parseOutputFormat(String format) {
        try {
            return OutputFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid output format: " + format);
            return OutputFormat.MARKDOWN;
        }
    }
}
