package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.cache.execute.ResultSender;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

public class GemfireFunctionHelper {
    public static void rethrowFunctionExceptions(ResultSender resultSender) {
        Answer throwSentException = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                throw (Throwable) args[0];
            }
        };
        doAnswer(throwSentException).when(resultSender).sendException(any(Throwable.class));
    }
}
