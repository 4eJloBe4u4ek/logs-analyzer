package analyzer.output;

import analyzer.analyzer.StatisticsAggregator;
import analyzer.config.AnalyzerConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReportGenerator {
    private final AnalyzerConfig analyzerConfig;
    private final StatisticsAggregator statisticsAggregator;

    public ReportGenerator(AnalyzerConfig analyzerConfig, StatisticsAggregator statisticsAggregator) {
        this.analyzerConfig = analyzerConfig;
        this.statisticsAggregator = statisticsAggregator;
    }

    public void saveStatisticsToFile(OutputFormat format) throws IOException {
        Files.write(analyzerConfig.getOutputPath(), generateStatisticsReport(format), StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void printStatisticsToConsole(OutputFormat format) {
        generateStatisticsReport(format).forEach(System.out::println);
    }

    private List<String> generateStatisticsReport(OutputFormat format) {
        List<String> lines = new ArrayList<>();

        addSection(lines, "Общая информация", generateGeneralInfoTable(), format);
        addSection(lines, "Запрашиваемые ресурсы", generateResourcesTable(), format);
        addSection(lines, "Коды ответа", generateStatusCodesTable(), format);
        addSection(lines, "Http-методы", generateHttpMethodsTable(), format);
        addSection(lines, "Топ активных IP-адресов", generateIpTable(), format);

        return lines;
    }

    private void addSection(List<String> lines, String title, List<String> data, OutputFormat format) {
        addSelectionHeader(lines, title, format);
        lines.addAll(data);
        lines.add("\n");
    }

    private List<String> generateGeneralInfoTable() {
        return generateTable(List.of(
            new String[] {"Метрика", "Значение"},
            new String[] {"Источник данных", analyzerConfig.getDataSourceAsString()},
            new String[] {"Начальная дата", analyzerConfig.from().map(LocalDateTime::toString).orElse("-")},
            new String[] {"Конечная дата", analyzerConfig.to().map(LocalDateTime::toString).orElse("-")},
            new String[] {"Количество запросов", formatNumber(statisticsAggregator.totalRequests())},
            new String[] {"Средний размер ответа",
                formatNumberWithUnderscores(statisticsAggregator.getAverageResponseSize(), "b")},
            new String[] {"95p размера ответа",
                formatNumberWithUnderscores(statisticsAggregator.getPercentileResponseSize(95), "b")}
        ));
    }

    private List<String> generateResourcesTable() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[] {"Ресурс", "Количество"});
        statisticsAggregator.getTopResources(10)
            .forEach((key, value) -> data.add(new String[] {'`' + key + '`', formatNumber(value)}));
        return generateTable(data);
    }

    private List<String> generateStatusCodesTable() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[] {"Код", "Имя", "Количество"});
        statisticsAggregator.getTopStatusCodes(10).forEach((key, value) -> {
            String statusName = analyzerConfig.getStatusDescription(key);
            data.add(new String[] {String.valueOf(key), statusName, formatNumber(value)});
        });
        return generateTable(data);
    }

    private List<String> generateHttpMethodsTable() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[] {"Метод", "Количество"});
        statisticsAggregator.getTopHttpMethods(10)
            .forEach((key, value) -> data.add(new String[] {'`' + key + '`', formatNumber(value)}));
        return generateTable(data);
    }

    private List<String> generateIpTable() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[] {"IP-адрес", "Количество"});
        statisticsAggregator.getTopIpAddresses(10)
            .forEach((key, value) -> data.add((new String[] {key, formatNumber(value)})));
        return generateTable(data);
    }

    private void addSelectionHeader(List<String> lines, String title, OutputFormat format) {
        switch (format) {
            case MARKDOWN -> lines.add("#### " + title);
            case ADOC -> {
                lines.add("==== " + title);
                lines.add("|===");
            }
        }
    }

    private List<String> generateTable(List<String[]> data) {
        List<String> table = new ArrayList<>();
        int[] columnWidths = getMaxColumnWidths(data);
        OutputFormat format = analyzerConfig.format();

        String headerFormat = Arrays.stream(columnWidths)
            .mapToObj(width -> "| %-" + width + "s ")
            .collect(Collectors.joining()) + "|";
        if (format == OutputFormat.ADOC) {
            headerFormat = headerFormat.substring(0, headerFormat.length() - 1);
        }
        table.add(String.format(headerFormat, (Object[]) data.getFirst()));

        if (format == OutputFormat.MARKDOWN) {
            String separator = Arrays.stream(columnWidths)
                .mapToObj(width -> "|:" + "-".repeat(width) + ":")
                .collect(Collectors.joining()) + "|";
            table.add(separator);
        }

        for (int i = 1; i < data.size(); i++) {
            table.add(String.format(headerFormat, (Object[]) data.get(i)));
        }

        if (format == OutputFormat.ADOC) {
            table.add("|===");
        }

        return table;
    }

    private String formatNumber(int value) {
        return String.format(Locale.US, "%,d", value).replace(',', '_');
    }

    private String formatNumberWithUnderscores(int value, String suffix) {
        return formatNumber(value) + suffix;
    }

    private int[] getMaxColumnWidths(List<String[]> data) {
        int columns = data.getFirst().length;
        int[] maxWidths = new int[columns];

        for (String[] row : data) {
            for (int i = 0; i < columns; i++) {
                maxWidths[i] = Math.max(maxWidths[i], row[i].length());
            }
        }
        return maxWidths;
    }
}
