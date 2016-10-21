package plugin.go.nuget.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.ConnectionHandler;
import plugin.go.nuget.PackageConfigHandler;
import plugin.go.nuget.builders.RequestBuilder;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;

public class PackageConfigHandlerTest {

    PackageConfigHandler packageConfigHandler;
    ConnectionHandler connectionHandler;

    @Before
    public void setup() {
        connectionHandler = mock(ConnectionHandler.class);
        packageConfigHandler = new PackageConfigHandler(connectionHandler);
    }

    @Test
    public void shouldErrorWhenPackageIDisMissing() {
        Map requestBody = new RequestBuilder().withPackageConfiguration("").build();

        List errorList = packageConfigHandler.handleValidateConfiguration(requestBody);
        Assert.assertFalse(errorList.isEmpty());
        Assert.assertThat(errorList.get(0).toString(), containsString("Package ID cannot be empty"));
    }

    @Test
    public void shouldNotErrorWhenPackageConfigurationsAreValid() {
        Map requestBody = new RequestBuilder().withPackageConfiguration("SOME_ID").build();

        List errorList = packageConfigHandler.handleValidateConfiguration(requestBody);
        Assert.assertTrue(errorList.isEmpty());
    }

}