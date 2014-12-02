package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.*;

public class FakeCacheListener implements CacheListener<Object, Object> {

    @Override
    public void afterCreate(EntryEvent<Object, Object> entryEvent) {

    }

    @Override
    public void afterUpdate(EntryEvent<Object, Object> entryEvent) {

    }

    @Override
    public void afterInvalidate(EntryEvent<Object, Object> entryEvent) {

    }

    @Override
    public void afterDestroy(EntryEvent<Object, Object> entryEvent) {

    }

    @Override
    public void afterRegionInvalidate(RegionEvent<Object, Object> regionEvent) {

    }

    @Override
    public void afterRegionDestroy(RegionEvent<Object, Object> regionEvent) {

    }

    @Override
    public void afterRegionClear(RegionEvent<Object, Object> regionEvent) {

    }

    @Override
    public void afterRegionCreate(RegionEvent<Object, Object> regionEvent) {

    }

    @Override
    public void afterRegionLive(RegionEvent<Object, Object> regionEvent) {

    }

    @Override
    public void close() {

    }
}