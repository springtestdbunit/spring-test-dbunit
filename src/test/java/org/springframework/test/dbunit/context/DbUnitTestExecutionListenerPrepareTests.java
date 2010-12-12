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
package org.springframework.test.dbunit.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.dbunit.DataSetLoader;
import org.springframework.test.dbunit.ExtendedTestContextManager;
import org.springframework.test.dbunit.FlatXmlDataSetLoader;
import org.springframework.test.dbunit.annotation.DbUnitConfiguration;

/**
 * Tests for {@link DbUnitTestExecutionListener} prepare method.
 * 
 * @author Phillip Webb
 */
public class DbUnitTestExecutionListenerPrepareTests {

	private static ThreadLocal<ApplicationContext> applicationContextThreadLocal = new ThreadLocal<ApplicationContext>();
	private ApplicationContext applicationContext;
	private IDatabaseConnection databaseConnection;
	private DataSource dataSource;

	@Before
	public void setup() {
		this.applicationContext = mock(ApplicationContext.class);
		this.databaseConnection = mock(IDatabaseConnection.class);
		this.dataSource = mock(DataSource.class);
		DbUnitTestExecutionListenerPrepareTests.applicationContextThreadLocal.set(applicationContext);
	}

	private void addBean(String beanName, Object bean) {
		given(applicationContext.containsBean(beanName)).willReturn(true);
		given(applicationContext.getBean(beanName)).willReturn(bean);
	}

	@Test
	public void shouldUseSensibleDefaultsOnClassWithNoDbUnitConfiguration() throws Exception {
		addBean("dbUnitDatabaseConnection", databaseConnection);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(NoDbUnitConfiguration.class);
		testContextManager.prepareTestInstance();
		assertSame(databaseConnection, testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.CONNECTION_ATTRIBUTE));
		assertEquals(FlatXmlDataSetLoader.class, testContextManager.getTestContextAttribute(
				DbUnitTestExecutionListener.DATA_SET_LOADER_ATTRIBUTE).getClass());
	}

	@Test
	public void shouldTryBeanFactoryForCommonBeanNamesWithNoDbUnitConfiguration() throws Exception {
		testCommonBeanNames(NoDbUnitConfiguration.class);
	}

	@Test
	public void shouldTryBeanFactoryForCommonBeanNamesWithEmptyDatabaseConnection() throws Exception {
		testCommonBeanNames(EmptyDbUnitConfiguration.class);
	}

	private void testCommonBeanNames(Class<?> testClass) throws Exception {
		addBean("dataSource", dataSource);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(testClass);
		testContextManager.prepareTestInstance();
		verify(applicationContext).containsBean("dbUnitDatabaseConnection");
		verify(applicationContext).containsBean("dataSource");
		verify(applicationContext).getBean("dataSource");
		verifyNoMoreInteractions(applicationContext);
	}

	@Test
	public void shouldConvertDatasetDatabaseConnection() throws Exception {
		addBean("dataSource", dataSource);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(NoDbUnitConfiguration.class);
		testContextManager.prepareTestInstance();
		Object connection = testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.CONNECTION_ATTRIBUTE);
		assertEquals(DatabaseDataSourceConnection.class, connection.getClass());
	}

	@Test
	public void shouldFailIfNoDbConnectionBeanIsFound() throws Exception {
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(NoDbUnitConfiguration.class);
		try {
			testContextManager.prepareTestInstance();
		} catch (IllegalStateException e) {
			assertTrue(e.getMessage().startsWith("Unable to find a DB Unit database connection"));
		}
	}

	@Test
	public void shouldFailIfDatabaseConnectionOfWrongTypeIsFound() throws Exception {
		addBean("dbUnitDatabaseConnection", new Integer(0));
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(NoDbUnitConfiguration.class);
		try {
			testContextManager.prepareTestInstance();
		} catch (IllegalArgumentException e) {
			assertEquals("Object of class [java.lang.Integer] must be an instance of interface "
					+ "org.dbunit.database.IDatabaseConnection", e.getMessage());
		}
	}

	@Test
	public void shouldSupportAllDbUnitConfigurationAttributes() throws Exception {
		addBean("customBean", databaseConnection);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(CustomConfiguration.class);
		testContextManager.prepareTestInstance();
		verify(applicationContext).getBean("customBean");
		assertSame(databaseConnection, testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.CONNECTION_ATTRIBUTE));
		assertEquals(CustomDataSetLoader.class, testContextManager.getTestContextAttribute(
				DbUnitTestExecutionListener.DATA_SET_LOADER_ATTRIBUTE).getClass());
	}

	@Test
	public void shouldFailIfDatasetLoaderCannotBeCreated() throws Exception {
		addBean("dbUnitDatabaseConnection", databaseConnection);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(NonCreatableDataSetLoader.class);
		try {
			testContextManager.prepareTestInstance();
		} catch (IllegalArgumentException e) {
			assertEquals("Unable to create data set loader instance for class "
					+ "org.springframework.test.dbunit.context.DbUnitTestExecutionListenerPrepareTests$"
					+ "AbstractCustomDataSetLoader", e.getMessage());
		}
	}

	private static class LocalApplicationContextLoader implements ContextLoader {
		public String[] processLocations(Class<?> clazz, String... locations) {
			return new String[] {};
		}

		public ApplicationContext loadContext(String... locations) throws Exception {
			return applicationContextThreadLocal.get();
		}
	}

	public abstract static class AbstractCustomDataSetLoader implements DataSetLoader {
		public IDataSet loadDataSet(TestContext testContext, String location) throws Exception {
			return null;
		}
	}

	public static class CustomDataSetLoader extends AbstractCustomDataSetLoader {
	}

	@ContextConfiguration(loader = LocalApplicationContextLoader.class)
	@TestExecutionListeners(DbUnitTestExecutionListener.class)
	private static class NoDbUnitConfiguration {

	}

	@ContextConfiguration(loader = LocalApplicationContextLoader.class)
	@TestExecutionListeners(DbUnitTestExecutionListener.class)
	@DbUnitConfiguration
	private static class EmptyDbUnitConfiguration {

	}

	@ContextConfiguration(loader = LocalApplicationContextLoader.class)
	@TestExecutionListeners(DbUnitTestExecutionListener.class)
	@DbUnitConfiguration(databaseConnection = "customBean", dataSetLoader = CustomDataSetLoader.class)
	private static class CustomConfiguration {

	}

	@ContextConfiguration(loader = LocalApplicationContextLoader.class)
	@TestExecutionListeners(DbUnitTestExecutionListener.class)
	@DbUnitConfiguration(dataSetLoader = AbstractCustomDataSetLoader.class)
	private static class NonCreatableDataSetLoader {

	}
}
