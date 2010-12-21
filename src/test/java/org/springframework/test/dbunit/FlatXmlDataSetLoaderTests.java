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
package org.springframework.test.dbunit;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.dbunit.dataset.IDataSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestContext;

/**
 * Test case for {@link FlatXmlDataSetLoader}.
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
	public void shouldLoadFromRelativeFile() throws Exception {
		IDataSet dataset = loader.loadDataSet(testContext.getTestClass(), "test.xml");
		assertEquals("Sample", dataset.getTableNames()[0]);
	}

	@Test
	public void shouldReturnNullOnMissingFile() throws Exception {
		IDataSet dataset = loader.loadDataSet(testContext.getTestClass(), "doesnotexist.xml");
		assertNull(dataset);
	}
}
