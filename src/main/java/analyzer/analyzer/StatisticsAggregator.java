package analyzer.analyzer;

import analyzer.model.NginxLogEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class StatisticsAggregator {
    private static final double PERCENT_BASE = 100.;
    private final List<Integer> responseSizes;
    private final Map<Integer, Integer> logStatusCodes;
    private final Map<String, Integer> resources;
    private final Map<String, Integer> httpMethods;
    private final Map<String, Integer> ipAddresses;
    private int totalRequests;

    public StatisticsAggregator() {
        this.logStatusCodes = new HashMap<>();
        this.responseSizes = new ArrayList<>();
        this.resources = new HashMap<>();
        this.httpMethods = new HashMap<>();
        this.ipAddresses = new HashMap<>();
    }

    public void addLogEntry(NginxLogEntry entry) {
        addStatusCode(entry.statusCode());
        addResource(entry.resource());
        addHttpMethod(entry.httpMethod());
        addIpAddress(entry.clientIP());

        responseSizes.add(entry.bodyBytesSent());
        totalRequests++;
    }

    private void addStatusCode(int statusCode) {
        logStatusCodes.put(statusCode, logStatusCodes.getOrDefault(statusCode, 0) + 1);
    }

    private void addResource(String resource) {
        resources.put(resource, resources.getOrDefault(resource, 0) + 1);
    }

    private void addHttpMethod(String httpMethod) {
        httpMethods.put(httpMethod, httpMethods.getOrDefault(httpMethod, 0) + 1);
    }

    private void addIpAddress(String ipAddress) {
        ipAddresses.put(ipAddress, ipAddresses.getOrDefault(ipAddress, 0) + 1);
    }

    public int getPercentileResponseSize(int percentile) {
        if (responseSizes.isEmpty()) {
            return 0;
        }

        Collections.sort(responseSizes);
        int index = (int) (Math.ceil((percentile / PERCENT_BASE) * responseSizes.size()) - 1);
        return responseSizes.get(index);
    }

    public int getAverageResponseSize() {
        double average = responseSizes.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0);

        return (int) Math.round(average);
    }

    public Map<String, Integer> getTopResources(int count) {
        return getTopEntries(resources, count);
    }

    public Map<Integer, Integer> getTopStatusCodes(int count) {
        return getTopEntries(logStatusCodes, count);
    }

    public Map<String, Integer> getTopHttpMethods(int count) {
        return getTopEntries(httpMethods, count);
    }

    public Map<String, Integer> getTopIpAddresses(int count) {
        return getTopEntries(ipAddresses, count);
    }

    private <T, V extends Comparable<V>> Map<T, V> getTopEntries(Map<T, V> map, int topCount) {
        return map.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(topCount)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
}
