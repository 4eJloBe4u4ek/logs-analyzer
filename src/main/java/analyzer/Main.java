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

public class Main {
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
        Optional<String> filterField = Optional.empty();
        Optional<String> filterValue = Optional.empty();
        if (params.filterField().isPresent() && params.filterValue().isPresent()) {
            filterField = params.filterField();
            filterValue = params.filterValue();
        }

        Optional<LocalDateTime> from = parseDate(params.from());
        Optional<LocalDateTime> to = parseDate(params.to());

        OutputFormat format = parseOutputFormat(params.format().orElse("markdown"));

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
            ? new UrlDataSource(analyzerConfig.urlString().get())
            : new LocalFileDataSource(analyzerConfig.files());
    }

    private static Optional<LocalDateTime> parseDate(Optional<String> dateString) {
        if (dateString.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDateTime.parse(dateString.get(), DateTimeFormatter.ISO_DATE_TIME));
        } catch (DateTimeParseException e) {
            return Optional.of(LocalDate.parse(dateString.get(), DateTimeFormatter.ISO_DATE).atStartOfDay());
        }
    }

    private static OutputFormat parseOutputFormat(String format) {
        return switch (format.toLowerCase()) {
            case "adoc" -> OutputFormat.ADOC;
            case "markdown" -> OutputFormat.MARKDOWN;
            default -> OutputFormat.MARKDOWN;
        };
    }
}
