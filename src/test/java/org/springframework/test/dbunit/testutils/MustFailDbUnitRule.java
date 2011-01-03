/*
 * Copyright 2010 the original author or authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.test.dbunit.testutils;

import org.junit.Assert;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.springframework.test.dbunit.DbUnitRule;

/**
 * An extension of {@link DbUnitRule} that ensures that a test method has failed.
 * 
 * @author Phillip Webb
 */
public class MustFailDbUnitRule extends DbUnitRule {

	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		final Statement statement = super.apply(base, method, target);
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				Throwable ex = null;
				try {
					statement.evaluate();
				} catch (Throwable e) {
					ex = e;
				}
				Assert.assertNotNull("Test did not fail", ex);
			}
		};
	}

}
