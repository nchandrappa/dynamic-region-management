package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.CacheWriter;
import com.gemstone.gemfire.cache.CacheWriterException;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.RegionEvent;

public class FakeCacheWriter implements CacheWriter<Object, Object> {
    @Override
    public void beforeUpdate(EntryEvent<Object, Object> entryEvent) throws CacheWriterException {

    }

    @Override
    public void beforeCreate(EntryEvent<Object, Object> entryEvent) throws CacheWriterException {

    }

    @Override
    public void beforeDestroy(EntryEvent<Object, Object> entryEvent) throws CacheWriterException {

    }

    @Override
    public void beforeRegionDestroy(RegionEvent<Object, Object> regionEvent) throws CacheWriterException {

    }

    @Override
    public void beforeRegionClear(RegionEvent<Object, Object> regionEvent) throws CacheWriterException {

    }

    @Override
    public void close() {

    }
}