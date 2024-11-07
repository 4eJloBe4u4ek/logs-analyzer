package analyzer.datasource;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UrlDataSourceTest {
    @Test
    public void getDataStream() {
        UrlDataSource urlDataSource = mock(UrlDataSource.class);

        when(urlDataSource.getDataStream()).thenReturn(Stream.of("some log entries..."));

        assertTrue(urlDataSource.getDataStream().findAny().isPresent());
    }
}
