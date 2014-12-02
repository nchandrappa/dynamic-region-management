package io.pivotal.adp_dynamic_region_management.options;

import com.gemstone.gemfire.cache.CacheWriter;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import io.pivotal.adp_dynamic_region_management.RegionOptionsInvalidException;

public class CacheWriterOption extends RegionOption<CacheWriter> {
    public CacheWriterOption(PdxInstance serverOptions) {
        super(serverOptions);
    }

    @Override
    public void setOptionOnRegionFactory(RegionFactory regionFactory) throws RegionOptionsInvalidException {
        regionFactory.setCacheWriter(value());
    }

    @Override
    protected CacheWriter value() throws RegionOptionsInvalidException {
        String cacheWriterClassName = (String) serverOptions.getField(getFieldName());

        Class cacheWriterClass;
        try {
            cacheWriterClass = Class.forName(cacheWriterClassName);
        } catch (ClassNotFoundException e) {
            throw new RegionOptionsInvalidException(
                    String.format("Invalid cacheWriter class name: `%s`.", cacheWriterClassName), e);
        }
        try {
            return (CacheWriter) cacheWriterClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RegionOptionsInvalidException(
                    String.format("Unable to instantiate cacheWriter: `%s`.", cacheWriterClassName), e);
        }
    }

    @Override
    protected String getFieldName() {
        return "cacheWriter";
    }
}
