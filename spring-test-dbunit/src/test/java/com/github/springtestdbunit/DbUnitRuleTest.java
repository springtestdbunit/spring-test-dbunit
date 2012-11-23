/*
 * Copyright 2010-2012 the original author or authors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.database.IDatabaseConnection;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;

import com.github.springtestdbunit.DbUnitRule.DbUnitTestContextAdapter;
import com.github.springtestdbunit.dataset.DataSetLoader;
import com.github.springtestdbunit.dataset.FlatXmlDataSetLoader;
import com.github.springtestdbunit.operation.DatabaseOperationLookup;
import com.github.springtestdbunit.operation.DefaultDatabaseOperationLookup;

public class DbUnitRuleTest {

	@Test
	public void shouldGetTestMethod() throws Exception {
		Blank target = new Blank();
		Method method = target.getClass().getMethod("test");
		FrameworkMethod frameworkMethod = new FrameworkMethod(method);
		DbUnitTestContextAdapter dbUnitTestContextAdapter = new DbUnitRule().new DbUnitTestContextAdapter(
				frameworkMethod, target);
		assertSame(method, dbUnitTestContextAdapter.getTestMethod());
	}

	@Test
	public void shouldUseSetDataSource() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		Blank target = new Blank();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitRule rule = new DbUnitRule();
		rule.setDataSource(dataSource);
		DbUnitTestContextAdapter dbUnitTestContextAdapter = rule.new DbUnitTestContextAdapter(method, target);
		dbUnitTestContextAdapter.getConnection().getConnection().createStatement();
		verify(connection).createStatement();
	}

	@Test
	public void shouldUseSetDatabaseConnection() throws Exception {
		IDatabaseConnection connection = mock(IDatabaseConnection.class);
		Blank target = new Blank();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitRule rule = new DbUnitRule();
		rule.setDatabaseConnection(connection);
		DbUnitTestContextAdapter dbUnitTestContextAdapter = rule.new DbUnitTestContextAdapter(method, target);
		assertSame(connection, dbUnitTestContextAdapter.getConnection());
	}

	@Test
	public void shouldUseSetDataSetLoader() throws Exception {
		DataSetLoader dataSetLoader = mock(DataSetLoader.class);
		Blank target = new Blank();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitRule rule = new DbUnitRule();
		rule.setDataSetLoader(dataSetLoader);
		DbUnitTestContextAdapter dbUnitTestContextAdapter = rule.new DbUnitTestContextAdapter(method, target);
		assertSame(dataSetLoader, dbUnitTestContextAdapter.getDataSetLoader());
	}

	@Test
	public void shouldUseSetDatabseOperationLookup() throws Exception {
		DatabaseOperationLookup lookup = mock(DatabaseOperationLookup.class);
		Blank target = new Blank();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitRule rule = new DbUnitRule();
		rule.setDatabaseOperationLookup(lookup);
		DbUnitTestContextAdapter dbUnitTestContextAdapter = rule.new DbUnitTestContextAdapter(method, target);
		assertSame(lookup, dbUnitTestContextAdapter.getDatbaseOperationLookup());
	}

	@Test
	public void shouldFindDataSourceFromTestCase() throws Exception {
		Connection connection = mock(Connection.class);
		WithDataSource target = new WithDataSource(connection);
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitTestContextAdapter dbUnitTestContextAdapter = new DbUnitRule().new DbUnitTestContextAdapter(method,
				target);
		dbUnitTestContextAdapter.getConnection().getConnection().createStatement();
		verify(connection).createStatement();
	}

	@Test
	public void shouldFindDatabaseConnectionFromTestCase() throws Exception {
		IDatabaseConnection connection = mock(IDatabaseConnection.class);
		WithDatabaseConnection target = new WithDatabaseConnection(connection);
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitTestContextAdapter dbUnitTestContextAdapter = new DbUnitRule().new DbUnitTestContextAdapter(method,
				target);
		assertSame(connection, dbUnitTestContextAdapter.getConnection());
	}

	@Test
	public void shouldFailIfNoConnection() throws Exception {
		Blank target = new Blank();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitTestContextAdapter dbUnitTestContextAdapter = new DbUnitRule().new DbUnitTestContextAdapter(method,
				target);
		try {
			dbUnitTestContextAdapter.getConnection();
		} catch (IllegalStateException e) {
			assertEquals("Unable to locate database connection for DbUnitRule.  "
					+ "Ensure that a DataSource or IDatabaseConnection is available as "
					+ "a private member of your test", e.getMessage());
		}
	}

	@Test
	public void shouldFailIfMultipleDataSources() throws Exception {
		WithMultipleDataSource target = new WithMultipleDataSource();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitTestContextAdapter dbUnitTestContextAdapter = new DbUnitRule().new DbUnitTestContextAdapter(method,
				target);
		try {
			dbUnitTestContextAdapter.getConnection().getConnection().createStatement();
		} catch (IllegalStateException e) {
			assertEquals(
					"Unable to read a single value from multiple fields of "
							+ "type javax.sql.DataSource from class com.github.springtestdbunit.DbUnitRuleTest$WithMultipleDataSource",
					e.getMessage());
		}

	}

	@Test
	public void shouldFindDataSetLoaderFromTestCase() throws Exception {
		WithDataSetLoader target = new WithDataSetLoader();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitTestContextAdapter dbUnitTestContextAdapter = new DbUnitRule().new DbUnitTestContextAdapter(method,
				target);
		assertSame(target.loader, dbUnitTestContextAdapter.getDataSetLoader());
	}

	@Test
	public void shouldFindDatabaseOperationLookupFromTestCase() throws Exception {
		WithDatabaseOperationLookup target = new WithDatabaseOperationLookup();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitTestContextAdapter dbUnitTestContextAdapter = new DbUnitRule().new DbUnitTestContextAdapter(method,
				target);
		assertSame(target.lookup, dbUnitTestContextAdapter.getDatbaseOperationLookup());
	}

	@Test
	public void shouldUseXmlDataSetLoaderIfNotSet() throws Exception {
		Blank target = new Blank();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitTestContextAdapter dbUnitTestContextAdapter = new DbUnitRule().new DbUnitTestContextAdapter(method,
				target);
		DataSetLoader loader = dbUnitTestContextAdapter.getDataSetLoader();
		assertNotNull(loader);
		assertEquals(FlatXmlDataSetLoader.class, loader.getClass());
	}

	@Test
	public void shouldUseDefaultDatabaseOperationLookupIfNotSet() throws Exception {
		Blank target = new Blank();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitTestContextAdapter dbUnitTestContextAdapter = new DbUnitRule().new DbUnitTestContextAdapter(method,
				target);
		DatabaseOperationLookup lookup = dbUnitTestContextAdapter.getDatbaseOperationLookup();
		assertNotNull(lookup);
		assertEquals(DefaultDatabaseOperationLookup.class, lookup.getClass());
	}

	static class Blank {
		public void test() {
		}
	}

	static class WithDataSource extends Blank {
		private DataSource dataSource;

		public WithDataSource(Connection connection) throws SQLException {
			this.dataSource = mock(DataSource.class);
			when(this.dataSource.getConnection()).thenReturn(connection);
		}
	}

	static class WithDatabaseConnection extends Blank {
		@SuppressWarnings("unused")
		private IDatabaseConnection databaseConnection;

		public WithDatabaseConnection(IDatabaseConnection databaseConnection) {
			this.databaseConnection = databaseConnection;
		}
	}

	static class WithMultipleDataSource extends Blank {
		@SuppressWarnings("unused")
		private DataSource dataSource1;
		@SuppressWarnings("unused")
		private DataSource dataSource2;
	}

	static class WithDataSetLoader extends Blank {
		private DataSetLoader loader = mock(DataSetLoader.class);
	}

	static class WithDatabaseOperationLookup extends Blank {
		private DatabaseOperationLookup lookup = mock(DatabaseOperationLookup.class);
	}
}
