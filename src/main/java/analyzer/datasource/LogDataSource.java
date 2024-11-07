package analyzer.datasource;

import java.util.stream.Stream;

public interface LogDataSource {
    Stream<String> getDataStream();
}
