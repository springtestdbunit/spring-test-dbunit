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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.TestExecutionListeners;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.dataset.DataSetLoader;
import com.github.springtestdbunit.dataset.FlatXmlDataSetLoader;
import com.github.springtestdbunit.operation.DatabaseOperationLookup;
import com.github.springtestdbunit.operation.DefaultDatabaseOperationLookup;
import com.github.springtestdbunit.testutils.ExtendedTestContextManager;

/**
 * Tests for {@link DbUnitTestExecutionListener} prepare method.
 *
 * @author Phillip Webb
 */
public class DbUnitTestExecutionListenerPrepareTest {

	private static ThreadLocal<ApplicationContext> applicationContextThreadLocal = new ThreadLocal<ApplicationContext>();

	private ApplicationContext applicationContext;

	private IDatabaseConnection databaseConnection;

	private DataSource dataSource;

	@Before
	public void setup() {
		this.applicationContext = mock(ApplicationContext.class);
		this.databaseConnection = mock(IDatabaseConnection.class);
		this.dataSource = mock(DataSource.class);
		DbUnitTestExecutionListenerPrepareTest.applicationContextThreadLocal.set(this.applicationContext);
	}

	@SuppressWarnings("unchecked")
	private void addBean(String beanName, Object bean) {
		given(this.applicationContext.containsBean(beanName)).willReturn(true);
		given(this.applicationContext.getBean(beanName)).willReturn(bean);
		given(this.applicationContext.getBean(eq(beanName), (Class) any())).willReturn(bean);
	}

	@Test
	public void shouldUseSensibleDefaultsOnClassWithNoDbUnitConfiguration() throws Exception {
		addBean("dbUnitDatabaseConnection", this.databaseConnection);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(NoDbUnitConfiguration.class);
		testContextManager.prepareTestInstance();
		DatabaseConnections databaseConnections = (DatabaseConnections) testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.CONNECTION_ATTRIBUTE);
		assertSame(this.databaseConnection, databaseConnections.get("dbUnitDatabaseConnection"));
		assertEquals(FlatXmlDataSetLoader.class, testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.DATA_SET_LOADER_ATTRIBUTE).getClass());
		assertEquals(DefaultDatabaseOperationLookup.class, testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.DATABASE_OPERATION_LOOKUP_ATTRIBUTE).getClass());
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
		addBean("dataSource", this.dataSource);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(testClass);
		testContextManager.prepareTestInstance();
		verify(this.applicationContext).containsBean("dbUnitDataSetLoader");
		verify(this.applicationContext).containsBean("dbUnitDatabaseConnection");
		verify(this.applicationContext).containsBean("dataSource");
		verify(this.applicationContext).getBean("dataSource");
		verifyNoMoreInteractions(this.applicationContext);
	}

	@Test
	public void shouldConvertDatasetDatabaseConnection() throws Exception {
		addBean("dataSource", this.dataSource);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(NoDbUnitConfiguration.class);
		testContextManager.prepareTestInstance();
		DatabaseConnections databaseConnections = (DatabaseConnections) testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.CONNECTION_ATTRIBUTE);
		Object connection = databaseConnections.get("dataSource");
		assertEquals(DatabaseDataSourceConnection.class, connection.getClass());
	}

	@Test
	public void shouldFailIfNoDbConnectionBeanIsFound() throws Exception {
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(NoDbUnitConfiguration.class);
		try {
			testContextManager.prepareTestInstance();
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().startsWith("Unable to find a DB Unit database connection"));
		}
	}

	@Test
	public void shouldFailIfDatabaseConnectionOfWrongTypeIsFound() throws Exception {
		addBean("dbUnitDatabaseConnection", new Integer(0));
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(NoDbUnitConfiguration.class);
		try {
			testContextManager.prepareTestInstance();
		} catch (IllegalArgumentException ex) {
			assertEquals("Object of class [java.lang.Integer] must be an instance of interface "
					+ "org.dbunit.database.IDatabaseConnection", ex.getMessage());
		}
	}

	@Test
	public void shouldSupportAllDbUnitConfigurationAttributes() throws Exception {
		addBean("customBean", this.databaseConnection);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(CustomConfiguration.class);
		testContextManager.prepareTestInstance();
		verify(this.applicationContext).getBean("customBean");
		DatabaseConnections databaseConnections = (DatabaseConnections) testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.CONNECTION_ATTRIBUTE);
		assertSame(this.databaseConnection, databaseConnections.get("customBean"));
		assertEquals(CustomDataSetLoader.class, testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.DATA_SET_LOADER_ATTRIBUTE).getClass());
		assertEquals(CustomDatabaseOperationLookup.class, testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.DATABASE_OPERATION_LOOKUP_ATTRIBUTE).getClass());
	}

	@Test
	public void shouldFailIfDatasetLoaderCannotBeCreated() throws Exception {
		addBean("dbUnitDatabaseConnection", this.databaseConnection);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(NonCreatableDataSetLoader.class);
		try {
			testContextManager.prepareTestInstance();
		} catch (IllegalArgumentException ex) {
			assertEquals("Unable to create data set loader instance for class "
					+ "com.github.springtestdbunit.DbUnitTestExecutionListenerPrepareTest$"
					+ "AbstractCustomDataSetLoader", ex.getMessage());
		}
	}

	@Test
	public void shouldSupportCustomLoaderBean() throws Exception {
		addBean("dataSource", this.dataSource);
		addBean("dbUnitDataSetLoader", new CustomDataSetLoader());
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(EmptyDbUnitConfiguration.class);
		testContextManager.prepareTestInstance();
		assertEquals(CustomDataSetLoader.class, testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.DATA_SET_LOADER_ATTRIBUTE).getClass());
	}

	@Test
	public void shouldSupportMetaAnnotations() throws Exception {
		addBean("customBean", this.databaseConnection);
		ExtendedTestContextManager testContextManager = new ExtendedTestContextManager(MetaAnnotatedConfiguration.class);
		testContextManager.prepareTestInstance();
		DatabaseConnections databaseConnections = (DatabaseConnections) testContextManager
				.getTestContextAttribute(DbUnitTestExecutionListener.CONNECTION_ATTRIBUTE);
		assertSame(this.databaseConnection, databaseConnections.get("customBean"));
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
		public IDataSet loadDataSet(Class<?> testClass, String location) throws Exception {
			return null;
		}
	}

	public static class CustomDataSetLoader extends AbstractCustomDataSetLoader {
	}

	public static class CustomDatabaseOperationLookup implements DatabaseOperationLookup {
		public org.dbunit.operation.DatabaseOperation get(DatabaseOperation operation) {
			return null;
		}
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
	@DbUnitConfiguration(databaseConnection = "customBean", dataSetLoader = CustomDataSetLoader.class, databaseOperationLookup = CustomDatabaseOperationLookup.class)
	private static class CustomConfiguration {

	}

	@ContextConfiguration(loader = LocalApplicationContextLoader.class)
	@TestExecutionListeners(DbUnitTestExecutionListener.class)
	@DbUnitConfiguration(dataSetLoader = AbstractCustomDataSetLoader.class)
	private static class NonCreatableDataSetLoader {

	}

	@ContextConfiguration(loader = LocalApplicationContextLoader.class)
	@TestExecutionListeners(DbUnitTestExecutionListener.class)
	@MetaAnnotation
	private static class MetaAnnotatedConfiguration {

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@DbUnitConfiguration(databaseConnection = "customBean")
	private @interface MetaAnnotation {

	}

}
