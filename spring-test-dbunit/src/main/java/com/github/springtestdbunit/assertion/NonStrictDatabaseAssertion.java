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
import java.util.LinkedList;
import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.Columns;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

/**
 * Implements non-strict database assertion strategy : compares data sets ignoring all tables and columns which are not
 * specified in expected data set but possibly exist in actual data set.
 * 
 * @author Mario Zagar
 */
class NonStrictDatabaseAssertion implements DatabaseAssertion {

	public void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
		if (expectedDataSet != actualDataSet) {
			for (String tableName : expectedDataSet.getTableNames()) {
				ITable expected = expectedDataSet.getTable(tableName);
				ITable actual = actualDataSet.getTable(tableName);
				String[] ignoredColumns = getColumnsToIgnore(expected.getTableMetaData(), actual.getTableMetaData());
				Assertion.assertEqualsIgnoreCols(expected, actual, ignoredColumns);
			}
		}
	}

    public void assertEqualsByQuery(IDataSet expectedDataSet, IDatabaseConnection connection,
                                    String sqlQuery, String tableName) throws DatabaseUnitException, SQLException {
        ITable expectedTable = expectedDataSet.getTable(tableName);
        ITable actualTable = connection.createQueryTable(tableName, sqlQuery);
        String[] ignoredColumns = getColumnsToIgnore(expectedTable.getTableMetaData(), actualTable.getTableMetaData());
        Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, ignoredColumns);
    }
	
	private String[] getColumnsToIgnore(ITableMetaData expectedMetaData, ITableMetaData actualMetaData)
			throws DataSetException {
		Column[] notSpecifiedInExpected = Columns.getColumnDiff(expectedMetaData, actualMetaData).getActual();
		List<String> result = new LinkedList<String>();
		for (Column column : notSpecifiedInExpected) {
			result.add(column.getColumnName());
		}
		return result.toArray(new String[result.size()]);
	}

}
