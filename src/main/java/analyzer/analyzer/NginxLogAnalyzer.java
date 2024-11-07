package analyzer.analyzer;

import analyzer.config.AnalyzerConfig;
import analyzer.datasource.LogDataSource;
import analyzer.model.NginxLogEntry;
import java.util.Optional;
import lombok.Getter;

@Getter
public class NginxLogAnalyzer {
    private final AnalyzerConfig analyzerConfig;
    private final LogDataSource dataSource;
    private final StatisticsAggregator statisticsAggregator;
    private final LogFilter logFilter;

    public NginxLogAnalyzer(AnalyzerConfig analyzerConfig, LogDataSource dataSource) {
        this.analyzerConfig = analyzerConfig;
        this.dataSource = dataSource;
        this.statisticsAggregator = new StatisticsAggregator();
        this.logFilter = new LogFilter(analyzerConfig);
    }

    public void analyze() {
        dataSource.getDataStream()
            .map(NginxLogEntry::parseNginxLogEntry)
            .flatMap(Optional::stream)
            .filter(entry -> logFilter.isWithinRange(entry.localDateTime()))
            .filter(logFilter::matchByFieldAndValue)
            .forEach(statisticsAggregator::addLogEntry);
    }
}
