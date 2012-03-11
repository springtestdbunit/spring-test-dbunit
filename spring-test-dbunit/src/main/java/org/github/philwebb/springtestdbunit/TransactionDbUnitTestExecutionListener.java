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
package org.github.philwebb.springtestdbunit;

import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.dbunit.annotation.DatabaseSetup;
import org.springframework.test.dbunit.annotation.DatabaseTearDown;
import org.springframework.test.dbunit.annotation.ExpectedDatabase;

/**
 * <code>TestExecutionListener</code> which provides support for {@link DatabaseSetup &#064;DatabaseSetup},
 * {@link DatabaseTearDown &#064;DatabaseTearDown} and {@link ExpectedDatabase &#064;ExpectedDatabase} annotations and
 * executed tests within {@link TransactionalTestExecutionListener transactions}.
 * <p>
 * Transactions start before {@link DatabaseSetup &#064;DatabaseSetup} and end after {@link DatabaseTearDown
 * &#064;DatabaseTearDown} and {@link ExpectedDatabase &#064;ExpectedDatabase}.
 * 
 * @see TransactionalTestExecutionListener
 * @see DbUnitTestExecutionListener
 * @author Phillip Webb
 */
public class TransactionDbUnitTestExecutionListener extends TestExecutionListenerChain {

	private static final Class<?>[] CHAIN = { TransactionalTestExecutionListener.class,
			DbUnitTestExecutionListener.class };

	@Override
	protected Class<?>[] getChain() {
		return CHAIN;
	}
}
