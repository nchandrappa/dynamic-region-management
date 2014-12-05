package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.pdx.PdxInstance;
import com.gemstone.gemfire.pdx.PdxInstanceFactory;
import com.gemstone.gemfire.pdx.WritablePdxInstance;

import java.util.List;

public class PdxInstanceHelper {
    static PdxInstance setValue(PdxInstance originalPdxInstance, String fieldNameToUpdate, Object newValue) {
        PdxInstance newPdxInstance;

        if(originalPdxInstance.hasField(fieldNameToUpdate)) {
            WritablePdxInstance writablePdxInstance = originalPdxInstance.createWriter();
            writablePdxInstance.setField(fieldNameToUpdate, newValue);

            newPdxInstance = writablePdxInstance;
        } else {
            PdxInstanceFactory pdxInstanceFactory = CacheFactory.getAnyInstance().createPdxInstanceFactory("");

            List<String> fieldNames = originalPdxInstance.getFieldNames();

            for (String fieldName : fieldNames) {
                Object fieldValue = originalPdxInstance.getField(fieldName);
                pdxInstanceFactory.writeObject(fieldName, fieldValue);
            }

            pdxInstanceFactory.writeObject(fieldNameToUpdate, newValue);

            newPdxInstance = pdxInstanceFactory.create();
        }

        return newPdxInstance;
    }
}
