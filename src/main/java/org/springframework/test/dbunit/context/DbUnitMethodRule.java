package org.springframework.test.dbunit.context;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class DbUnitMethodRule implements MethodRule {

	private static DbUnitRunner runner = new DbUnitRunner();
	
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		return base;
		//FIXME
	}
	
	
}
