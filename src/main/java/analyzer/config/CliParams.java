package analyzer.config;

import com.beust.jcommander.Parameter;
import java.util.Optional;

public class CliParams {

    @Parameter(names = {"--path"}, description = "Path to the log files directory", required = true)
    private String path;

    @Parameter(names = {"--from"}, description = "Start date in yyyy-MM-dd format")
    private String from;

    @Parameter(names = {"--to"}, description = "End date in yyyy-MM-dd format")
    private String to;

    @Parameter(names = {"--format"}, description = "Output format (markdown or adoc)")
    private String format;

    @Parameter(names = {"--filter-field"}, description = "The field to filter logs by ('agent', 'method'...)")
    private String filterField;

    @Parameter(names = {"--filter-value"}, description = "The value to filter logs for ('Mozilla*', 'GET'...)")
    private String filterValue;

    public Optional<String> from() {
        return Optional.ofNullable(from);
    }

    public Optional<String> to() {
        return Optional.ofNullable(to);
    }

    public Optional<String> format() {
        return Optional.ofNullable(format);
    }

    public String path() {
        return path;
    }

    public Optional<String> filterField() {
        return Optional.ofNullable(filterField);
    }

    public Optional<String> filterValue() {
        return Optional.ofNullable(filterValue);
    }
}
