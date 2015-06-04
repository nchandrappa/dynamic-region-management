package io.pivotal.adp_dynamic_region_management;

import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;

public class TestFunction extends FunctionAdapter implements Declarable {

    public void execute(FunctionContext fc) {
        fc.getResultSender().lastResult("TestFunction succeeded.");
    }

    public String getId() {
        return getClass().getName();
    }

	@Override
	public void init(Properties arg0) {
	}
}
