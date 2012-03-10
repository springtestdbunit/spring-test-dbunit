package org.springframework.test.dbunit;

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
import org.springframework.test.dbunit.DbUnitRule.DbUnitTestContextAdapter;
import org.springframework.test.dbunit.dataset.DataSetLoader;
import org.springframework.test.dbunit.dataset.FlatXmlDataSetLoader;

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
							+ "type javax.sql.DataSource from class org.springframework.test.dbunit.DbUnitRuleTest$WithMultipleDataSource",
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
	public void shouldUseXmlDataSetLoaderIfNotSet() throws Exception {
		Blank target = new Blank();
		FrameworkMethod method = new FrameworkMethod(target.getClass().getMethod("test"));
		DbUnitTestContextAdapter dbUnitTestContextAdapter = new DbUnitRule().new DbUnitTestContextAdapter(method,
				target);
		DataSetLoader loader = dbUnitTestContextAdapter.getDataSetLoader();
		assertNotNull(loader);
		assertEquals(FlatXmlDataSetLoader.class, loader.getClass());
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
}
