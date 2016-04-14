/*
 * Copyright 2002-2013 the original author or authors
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
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.IColumnFilter;

/**
 * Database assertion strategy interface.
 *
 * @author Mario Zagar
 * @author Sunitha Rajarathnam
 */
public interface DatabaseAssertion {

	/**
	 * Assert that the specified {@link IDataSet dataSets} are conceptually equal.
	 * @param expectedDataSet the expected dataset
	 * @param actualDataSet the actual dataset
	 * @param columnFilters any column filters to apply
	 * @throws DatabaseUnitException if the datasets are not equal
	 */
	void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet, List<IColumnFilter> columnFilters)
			throws DatabaseUnitException;

	/**
	 * Assert that the specified {@link IDataSet dataSets} are conceptually equal.
	 * @param expectedTable the expected table
	 * @param actualTable the actual table
	 * @param columnFilters any column filters to apply
	 * @throws DatabaseUnitException if the tables are not equal
	 */
	void assertEquals(ITable expectedTable, ITable actualTable, List<IColumnFilter> columnFilters)
			throws DatabaseUnitException;

}
