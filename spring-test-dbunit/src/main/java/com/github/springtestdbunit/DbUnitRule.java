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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.springframework.util.ReflectionUtils;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import com.github.springtestdbunit.dataset.DataSetLoader;
import com.github.springtestdbunit.dataset.FlatXmlDataSetLoader;
import com.github.springtestdbunit.operation.DatabaseOperationLookup;
import com.github.springtestdbunit.operation.DefaultDatabaseOperationLookup;

/**
 * JUnit <code>&#064;Rule</code> which provides support for {@link DatabaseSetup &#064;DatabaseSetup},
 * {@link DatabaseTearDown &#064;DatabaseTearDown} and {@link ExpectedDatabase &#064;ExpectedDatabase} annotations.
 * <p>
 * The fields of the test class will inspected to locate the {@link IDatabaseConnection} or {@link DataSource} to use.
 * Generally, a single <code>&#064;Autowired</code> field is expected. It is also possible to configure the connection
 * directly using the {@link #setDatabaseConnection} and {@link #setDataSource} methods.
 * <p>
 * Datasets are loaded using the {@link FlatXmlDataSetLoader} unless a loader is located from a field of the test class
 * or specifically {@link #setDataSetLoader configured}.
 * <p>
 * Database operations are are lookup up using a {@link DefaultDatabaseOperationLookup} unless a
 * {@link DatabaseOperationLookup} is located from a field of the test class or specifically
 * {@link #setDatabaseOperationLookup configured}.
 * 
 * @author Phillip Webb
 */
public class DbUnitRule implements MethodRule {

	private static DbUnitRunner runner = new DbUnitRunner();

	private static Map<Class<?>, TestClassFields> fields = new HashMap<Class<?>, DbUnitRule.TestClassFields>();

	private IDatabaseConnection connection;

	private DataSetLoader dataSetLoader;

	private DatabaseOperationLookup databaseOperationLookup;

	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		DbUnitTestContextAdapter context = new DbUnitTestContextAdapter(method, target);
		return new DbUnitStatement(context, base);
	}

	/**
	 * Set the {@link DataSource} that will be used when running DBUnit tests. Note: Setting a data source will replace
	 * any previously configured {@link #setDatabaseConnection(IDatabaseConnection) connection}.
	 * @param dataSource The data source
	 */
	public void setDataSource(DataSource dataSource) {
		this.connection = DatabaseDataSourceConnectionFactoryBean.newConnection(dataSource);
	}

	/**
	 * Set the {@link IDatabaseConnection} that will be used when running DBUnit tests. Note: Setting a connection will
	 * replace any previously configured {@link #setDatabaseConnection(IDatabaseConnection) dataSource}.
	 * @param connection The connection
	 */
	public void setDatabaseConnection(IDatabaseConnection connection) {
		this.connection = connection;
	}

	/**
	 * Set the {@link DataSetLoader} that will be used to load {@link IDataSet}s.
	 * @param dataSetLoader The data set loader
	 */
	public void setDataSetLoader(DataSetLoader dataSetLoader) {
		this.dataSetLoader = dataSetLoader;
	}

	/**
	 * Set the {@link DatabaseOperationLookup} that will be used to lookup DBUnit databsae operations.
	 * @param databaseOperationLookup the database operation lookup
	 */
	public void setDatabaseOperationLookup(DatabaseOperationLookup databaseOperationLookup) {
		this.databaseOperationLookup = databaseOperationLookup;
	}

	private static TestClassFields getTestClassFields(Class<?> testClass) {
		TestClassFields fields = DbUnitRule.fields.get(testClass);
		if (fields == null) {
			fields = new TestClassFields(testClass);
			DbUnitRule.fields.put(testClass, fields);
		}
		return fields;
	}

	protected class DbUnitTestContextAdapter implements DbUnitTestContext {

		private FrameworkMethod method;
		private Object target;
		private Throwable testException;

		public DbUnitTestContextAdapter(FrameworkMethod method, Object target) {
			this.method = method;
			this.target = target;
		}

		private boolean hasField(Class<?> type) {
			return getField(type) != null;
		}

		private <T> T getField(Class<T> type) {
			return getTestClassFields(getTestClass()).get(type, this.target);
		}

		public IDatabaseConnection getConnection() {
			if (DbUnitRule.this.connection == null) {
				if (hasField(IDatabaseConnection.class)) {
					DbUnitRule.this.connection = getField(IDatabaseConnection.class);
				} else if (hasField(DataSource.class)) {
					DbUnitRule.this.connection = DatabaseDataSourceConnectionFactoryBean
							.newConnection(getField(DataSource.class));
				} else {
					throw new IllegalStateException(
							"Unable to locate database connection for DbUnitRule.  Ensure that a DataSource or IDatabaseConnection "
									+ "is available as a private member of your test");
				}
			}
			return DbUnitRule.this.connection;
		}

		public DataSetLoader getDataSetLoader() {
			if (DbUnitRule.this.dataSetLoader == null) {
				if (hasField(DataSetLoader.class)) {
					DbUnitRule.this.dataSetLoader = getField(DataSetLoader.class);
				} else {
					DbUnitRule.this.dataSetLoader = new FlatXmlDataSetLoader();
				}
			}
			return DbUnitRule.this.dataSetLoader;
		}

		public DatabaseOperationLookup getDatbaseOperationLookup() {
			if (DbUnitRule.this.databaseOperationLookup == null) {
				if (hasField(DatabaseOperationLookup.class)) {
					DbUnitRule.this.databaseOperationLookup = getField(DatabaseOperationLookup.class);
				} else {
					DbUnitRule.this.databaseOperationLookup = new DefaultDatabaseOperationLookup();
				}
			}
			return DbUnitRule.this.databaseOperationLookup;
		}

		public Class<?> getTestClass() {
			return this.target.getClass();
		}

		public Method getTestMethod() {
			return this.method.getMethod();
		}

		public Throwable getTestException() {
			return this.testException;
		}

		public void setTestException(Throwable e) {
			this.testException = e;
		}
	}

	private class DbUnitStatement extends Statement {

		private Statement nextStatement;
		private DbUnitTestContextAdapter testContext;

		public DbUnitStatement(DbUnitTestContextAdapter testContext, Statement nextStatement) {
			this.testContext = testContext;
			this.nextStatement = nextStatement;
		}

		@Override
		public void evaluate() throws Throwable {
			runner.beforeTestMethod(this.testContext);
			try {
				this.nextStatement.evaluate();
			} catch (Throwable e) {
				this.testContext.setTestException(e);
				throw e;
			} finally {
				runner.afterTestMethod(this.testContext);
			}
		}
	}

	private static class TestClassFields {

		private Map<Class<?>, Set<Field>> fieldMap = new HashMap<Class<?>, Set<Field>>();

		private Class<?> testClass;

		public TestClassFields(Class<?> testClass) {
			this.testClass = testClass;
		}

		private Set<Field> getFields(final Class<?> type) {
			if (this.fieldMap.containsKey(type)) {
				return this.fieldMap.get(type);
			}
			final Set<Field> fields = new HashSet<Field>();
			ReflectionUtils.doWithFields(this.testClass, new ReflectionUtils.FieldCallback() {
				public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
					if (type.isAssignableFrom(field.getType())) {
						field.setAccessible(true);
						fields.add(field);
					}
				}
			});
			this.fieldMap.put(type, fields);
			return fields;
		}

		@SuppressWarnings("unchecked")
		public <T> T get(Class<T> type, Object obj) {
			Set<Field> fields = getFields(type);
			switch (fields.size()) {
			case 0:
				return null;
			case 1:
				try {
					return (T) fields.iterator().next().get(obj);
				} catch (Exception e) {
					throw new IllegalStateException("Unable to read field of type " + type.getName() + " from "
							+ this.testClass, e);
				}
			}
			throw new IllegalStateException("Unable to read a single value from multiple fields of type "
					+ type.getName() + " from " + this.testClass);
		}
	}
}
