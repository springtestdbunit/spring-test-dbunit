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
package com.github.springtestdbunit.assertion;

import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;

/**
 * Database assertion strategy interface.
 * 
 * @author Mario Zagar
 */
public interface DatabaseAssertion {

	/**
	 * Assert that the specified {@link IDataSet dataSets} are conceptually equal.
	 * @param expectedDataSet the expected dataset
	 * @param actualDataSet the actual dataset
	 * @throws DatabaseUnitException if the datasets are not equal
	 */
	void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException;

	/**
	 * Assert that the table from the dataset and the table generated from the sql query are conceptually equal.
	 * 
	 * @param expectedDataSet the expected dataset
	 * @param connection the IDatabaseConnection
	 * @param sqlQuery query that is used to generate the actual table data to be compared.
	 * @param tableName name of the table that is compared.
	 * @throws DatabaseUnitException if the tables data are not equal.
	 * @throws SQLException 
	 */
	void assertEqualsByQuery(IDataSet expectedDataSet, IDatabaseConnection connection, String sqlQuery, String tableName) throws DatabaseUnitException, SQLException;
}
