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

package com.github.springtestdbunit.assertion;

import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.IColumnFilter;

/**
 * Implements non-strict unordered database assertion strategy : compares data sets ignoring all tables and columns
 * which are not specified in expected data set but possibly exist in actual data set and sorting rows in expected and
 * actual data sets with column order in expected data set to ignore row orders in expected and actual data sets.
 *
 * @author Mario Zagar
 * @author Sunitha Rajarathnam
 * @author Mehmet Aslan
 */
class NonStrictUnorderedDatabaseAssertion extends NonStrictDatabaseAssertion {

	@Override
	public void assertEquals(ITable expectedSortedTable, ITable actualSortedTable, List<IColumnFilter> columnFilters)
			throws DatabaseUnitException {
		Column[] expectedColumns = expectedSortedTable.getTableMetaData().getColumns();
		expectedSortedTable = new SortedTable(expectedSortedTable, expectedColumns);
		actualSortedTable = new SortedTable(actualSortedTable, expectedColumns);
		super.assertEquals(expectedSortedTable, actualSortedTable, columnFilters);
	}

}
