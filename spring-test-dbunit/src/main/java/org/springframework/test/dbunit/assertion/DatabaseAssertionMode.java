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
package org.springframework.test.dbunit.assertion;

import org.springframework.test.dbunit.annotation.ExpectedDatabase;

/**
 * Database assertion modes which determine {@link ExpectedDatabase} behaviour.
 * 
 * @author Mario Zagar
 */
public enum DatabaseAssertionMode {

	/**
	 * Will use default DbUnit data sets assertions.
	 */
	DEFAULT(new DefaultDatabaseAssertion()),

	/**
	 * Allows specifying only specific columns and tables in expected data set. Unspecified tables and columns are
	 * ignored. </p> <strong>Notes:</strong>
	 * <ul>
	 * <li>Expected row order must match order in actual data set.</li>
	 * <li>Specified columns must match in all rows, e.g. specifying 'column1' value without 'column2' value in one row
	 * and only 'column2' value in another is not allowed - both 'column1' and 'column2' values must be specified in all
	 * rows.</li>
	 * </ul>
	 */
	NON_STRICT(new NonStrictDatabaseAssertion());

	private DatabaseAssertion databaseAssertion;

	private DatabaseAssertionMode(DatabaseAssertion databaseAssertion) {
		this.databaseAssertion = databaseAssertion;
	}

	public DatabaseAssertion getDatabaseAssertion() {
		return this.databaseAssertion;
	}
}