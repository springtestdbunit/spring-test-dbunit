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

package com.github.springtestdbunit.dataset;

import static org.junit.Assert.*;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestContext;

import com.github.springtestdbunit.testutils.ExtendedTestContextManager;

/**
 * Tests for {@link FlatXmlDataSetLoader}.
 * 
 * @author Phillip Webb
 */
public class FlatXmlDataSetLoaderTests {

	private TestContext testContext;

	private FlatXmlDataSetLoader loader;

	@Before
	public void setup() throws Exception {
		this.loader = new FlatXmlDataSetLoader();
		ExtendedTestContextManager manager = new ExtendedTestContextManager(getClass());
		this.testContext = manager.accessTestContext();
	}

	@Test
	public void shouldLoadMissingColumn() throws Exception {
		IDataSet dataset = this.loader.loadDataSet(this.testContext.getTestClass(), "test_null_missing_column.xml");
		assertEquals("test", dataset.getTable("Sample").getValue(1, "name"));
	}
	
	@Test
	public void shouldLoadNullValue() throws Exception {
		IDataSet dataset = this.loader.loadDataSet(this.testContext.getTestClass(), "test_null_missing_column.xml");
		assertEquals(null, dataset.getTable("Sample").getValue(0, "name"));
	}
	
	@Test
	public void shouldLoadFromRelativeFile() throws Exception {
		IDataSet dataset = this.loader.loadDataSet(this.testContext.getTestClass(), "test.xml");
		assertEquals("Sample", dataset.getTableNames()[0]);
	}

	@Test
	public void shouldReturnNullOnMissingFile() throws Exception {
		IDataSet dataset = this.loader.loadDataSet(this.testContext.getTestClass(), "doesnotexist.xml");
		assertNull(dataset);
	}
}
