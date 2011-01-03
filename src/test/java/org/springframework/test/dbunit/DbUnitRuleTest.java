package org.springframework.test.dbunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.database.IDatabaseConnection;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.springframework.test.dbunit.DbUnitRule.DbUnitTestContextAdapter;
import org.springframework.test.dbunit.dataset.DataSetLoader;

public class DbUnitRuleTest {

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
		// FIXME

	}

	@Test
	public void shouldFindDataSetLoaderFromTestCase() throws Exception {
		// FIXME

	}

	@Test
	public void shouldUseXmlDataSetLoaderIfNotSet() throws Exception {
		// FIXME

	}

	static class WithDataSource {
		DataSource dataSource;

		public WithDataSource(Connection connection) throws SQLException {
			dataSource = mock(DataSource.class);
			when(dataSource.getConnection()).thenReturn(connection);
		}

		public void test() {
		}
	}

	static class WithDatabaseConnection {
		IDatabaseConnection databaseConnection;

		public WithDatabaseConnection(IDatabaseConnection databaseConnection) {
			this.databaseConnection = databaseConnection;
		}

		public void test() {
		}
	}

	static class Blank {
		public void test() {
		}
	}
}
