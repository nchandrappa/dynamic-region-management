package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;

import io.pivotal.adp_dynamic_region_management.CacheSingleton;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;
import io.pivotal.adp_dynamic_region_management.options.ValueConstraintOption;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static junit.framework.TestCase.assertEquals;

public class ValueConstraintOptionTest {
    private static RegionFactory regionFactory;
    private static PdxInstance serverOptions;

    @BeforeClass
    static public void setUp() throws Exception {
        CacheSingleton.getCache();

        regionFactory = CacheFactory.getAnyInstance().createRegionFactory();

    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TestName name = new TestName();

    @Test
    public void testValueConstraintSetOnRegionFactory() throws Exception {
        serverOptions = JSONFormatter.fromJSON("{ \"valueConstraint\": \"" + String.class.getName() + "\" }");

        new ValueConstraintOption(serverOptions).setOptionOnRegionFactory(regionFactory);

        Region region = regionFactory.create(getCurrentTestName());

        assertEquals(Class.forName(String.class.getName()), region.getAttributes().getValueConstraint());
    }

    @Test
    public void testValueConstraintNotSetWhenClassDoesNotExist() throws Exception {
        serverOptions = JSONFormatter.fromJSON("{ \"valueConstraint\": \"does.not.exist.ClassName\" }");
        exception.expect(RegionOptionsInvalidException.class);
        exception.expectMessage("Could not find class: `does.not.exist.ClassName`.");

        new ValueConstraintOption(serverOptions).setOptionOnRegionFactory(regionFactory);
    }

    private String getCurrentTestName() {
        return getClass().getSimpleName() + name.getMethodName();
    }
}
