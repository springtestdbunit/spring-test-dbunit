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

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;

/**
 * Default database assertion strategy which uses DbUnit {@link Assertion#assertEquals(IDataSet, IDataSet)}.
 * 
 * @author Mario Zagar
 */
class DefaultDatabaseAssertion implements DatabaseAssertion {

	/**
	 * Uses DbUnit {@link Assertion#assertEquals(IDataSet, IDataSet)}.
	 */
	public void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
		Assertion.assertEquals(expectedDataSet, actualDataSet);
	}
}
