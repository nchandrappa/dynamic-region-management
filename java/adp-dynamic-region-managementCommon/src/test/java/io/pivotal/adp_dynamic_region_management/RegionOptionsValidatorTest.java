package io.pivotal.adp_dynamic_region_management;

import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;
import io.pivotal.adp_dynamic_region_management.RegionOptionsValidator;

import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class RegionOptionsValidatorTest {

    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testValidateReturnsTrueWhenClientTypePresent() throws RegionOptionsInvalidException {
        String jsonString = "{\n" +
                "    \"client\": {\n" +
                "        \"type\": \"LOCAL\"\n" +
                "    }\n" +
                "}";
        PdxInstance regionOptions = JSONFormatter.fromJSON(jsonString);
        RegionOptionsValidator validator = new RegionOptionsValidator(regionOptions);
        assertThat(validator.validate(), equalTo(true));
    }

    @Test
    public void testValidateReturnsFalseWhenClientTypeAbsent() throws RegionOptionsInvalidException {
        String jsonString = "{\n" +
                "    \"client\": {\n" +
                "    }\n" +
                "}";
        PdxInstance regionOptions = JSONFormatter.fromJSON(jsonString);
        RegionOptionsValidator validator = new RegionOptionsValidator(regionOptions);

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid region options. Expected client.type to be defined.");

        validator.validate();
    }

    @Test
    public void testValidateReturnsFalseWhenClientAbsent() throws RegionOptionsInvalidException {
        String jsonString = "{}";
        PdxInstance regionOptions = JSONFormatter.fromJSON(jsonString);
        RegionOptionsValidator validator = new RegionOptionsValidator(regionOptions);

        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Invalid region options. Expected client to be defined.");

        validator.validate();
    }
}