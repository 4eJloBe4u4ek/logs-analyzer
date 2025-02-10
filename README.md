**Description**

Manually processing and analyzing logs can be tedious, so this project provides a log analyzer program.

**The program accepts the following command-line arguments:**
* Path to one or more NGINX log files, either as a local pattern (glob) or a URL.
* Optional time range parameters (from and to) in ISO8601 format.
* Optional output format (markdown or adoc).
* Optional log filtering by value for fields such as agent, status, resource, method, and ip.

**Example Usage:**
```
--path
https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs
--filter-field
agent
--filter-value
"Mozilla*"
--from
2015-05-17
--to
2015-05-20
```


**Computes log statistics:**
* Total number of requests.
* Most frequently requested resources.
* Most common response codes.
* Average response size.
* 95th percentile response size.
* Top active IP addresses
