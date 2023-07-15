/*
 * Copyright 2016 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package plugin.go.nuget.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plugin.go.nuget.PackageConfigHandler;
import plugin.go.nuget.builders.RequestBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PackageConfigHandlerTest {

    PackageConfigHandler packageConfigHandler;

    @BeforeEach
    public void setup() {
        packageConfigHandler = new PackageConfigHandler();
    }

    @Test
    public void shouldErrorWhenPackageIDisMissing() {
        Map requestBody = new RequestBuilder().withPackageConfiguration("").build();

        List errorList = packageConfigHandler.handleValidateConfiguration(requestBody);
        assertFalse(errorList.isEmpty());
        assertThat(errorList.get(0).toString()).contains("Package ID cannot be empty");
    }

    @Test
    public void shouldNotErrorWhenPackageConfigurationsAreValid() {
        Map requestBody = new RequestBuilder().withPackageConfiguration("SOME_ID").build();

        List errorList = packageConfigHandler.handleValidateConfiguration(requestBody);
        assertTrue(errorList.isEmpty());
    }

}