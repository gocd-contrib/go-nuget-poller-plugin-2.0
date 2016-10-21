package plugin.go.nuget.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.ConnectionHandler;
import plugin.go.nuget.PackageConfigHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static utils.Constants.PACKAGE_CONFIGURATION;

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
        Map requestBody = createSampleRequestForPackageConfiguration("");

        List errorList = packageConfigHandler.handleValidatePackageConfiguration(requestBody);
        Assert.assertFalse(errorList.isEmpty());
        Assert.assertThat(errorList.get(0).toString(), containsString("Package ID cannot be empty"));
    }

    @Test
    public void shouldNotErrorWhenPackageConfigurationsAreValid() {
        Map requestBody = createSampleRequestForPackageConfiguration("ID");

        List errorList = packageConfigHandler.handleValidatePackageConfiguration(requestBody);
        Assert.assertTrue(errorList.isEmpty());
    }

    private Map createSampleRequestForPackageConfiguration(String packageID) {
        Map packageIDMap = new HashMap();
        packageIDMap.put("value", packageID);

        Map packageConfigurationMap = new HashMap();
        packageConfigurationMap.put("PACKAGE_ID", packageIDMap);

        Map requestMap = new HashMap();
        requestMap.put(PACKAGE_CONFIGURATION, packageConfigurationMap);

        return requestMap;
    }
}