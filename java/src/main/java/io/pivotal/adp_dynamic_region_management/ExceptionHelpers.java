package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.execute.FunctionContext;

public class ExceptionHelpers {
    static void sendStrippedException(FunctionContext context, Exception exception) {
        RuntimeException serializableException = new RuntimeException(exception.getMessage());
        serializableException.setStackTrace(exception.getStackTrace());
        context.getResultSender().sendException(serializableException);
    }
}
