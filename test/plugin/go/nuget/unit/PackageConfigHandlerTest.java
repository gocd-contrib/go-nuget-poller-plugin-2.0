package plugin.go.nuget.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.PackageConfigHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static utils.Constants.PACKAGE_CONFIGURATION;

public class PackageConfigHandlerTest {
    PackageConfigHandler packageConfigHandler;

    @Before
    public void setup(){
        packageConfigHandler = new PackageConfigHandler();
    }

    @Test
    public void shouldErrorWhenPackageIDisMissing(){
        Map requestBody = createPackageConfigurationRequestBody("");

        List errorList = packageConfigHandler.handleValidatePackageConfiguration(requestBody);
        Assert.assertFalse(errorList.isEmpty());
        Assert.assertThat(errorList.get(0).toString(), containsString("Package ID cannot be empty"));
    }

    @Test
    public void shouldNotErrorWhenPackageConfigurationsAreValid(){
        Map requestBody = createPackageConfigurationRequestBody("ID");

        List errorList = packageConfigHandler.handleValidatePackageConfiguration(requestBody);
        Assert.assertTrue(errorList.isEmpty());
    }

    private Map createPackageConfigurationRequestBody(String packageID) {
        Map packageIDMap = new HashMap();
        packageIDMap.put("value", packageID);

        Map packageConfigurationMap = new HashMap();
        packageConfigurationMap.put("PACKAGE_ID", packageIDMap);

        Map requestMap = new HashMap();
        requestMap.put(PACKAGE_CONFIGURATION, packageConfigurationMap);

        return requestMap;
    }
}