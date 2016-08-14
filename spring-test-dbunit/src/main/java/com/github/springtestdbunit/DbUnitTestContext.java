/*
 * Copyright 2002-2016 the original author or authors
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

package com.github.springtestdbunit;

import java.lang.reflect.Method;

import com.github.springtestdbunit.assertion.DatabaseAssertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;

import com.github.springtestdbunit.dataset.DataSetLoader;
import com.github.springtestdbunit.operation.DatabaseOperationLookup;

/**
 * Provides context for the {@link DbUnitRunner}.
 *
 * @author Phillip Webb
 */
public interface DbUnitTestContext {

	/**
	 * Returns the {@link IDatabaseConnection} that should be used when performing database setup and teardown.
	 * @return The connection
	 */
	DatabaseConnections getConnections();

	/**
	 * Returns the {@link DataSetLoader} that should be used to load {@link IDataSet}s.
	 * @return The dataset loader
	 */
	DataSetLoader getDataSetLoader();

	/**
	 * Returns the {@link DatabaseOperationLookup} that should be used to lookup database operations.
	 * @return the database operation lookup
	 */
	DatabaseOperationLookup getDatbaseOperationLookup();

	/**
	 * Returns the {@link DatabaseAssertion} implemented by the bean with the given name.
	 * @param databaseAssertionBeanName  name of the database assertion bean
	 * @return  database assertion
	 */
	DatabaseAssertion getDatabaseAssertion(String databaseAssertionBeanName);

	/**
	 * Returns the class that is under test.
	 * @return The class under test
	 */
	Class<?> getTestClass();

	/**
	 * Returns the instance that is under test.
	 * @return The instance under test
	 */
	Object getTestInstance();

	/**
	 * Returns the method that is under test.
	 * @return The method under test
	 */
	Method getTestMethod();

	/**
	 * Returns any exception that was thrown during the test or <tt>null</tt> if no test exception occurred.
	 * @return the test exception or <tt>null</tt>
	 */
	Throwable getTestException();

}
