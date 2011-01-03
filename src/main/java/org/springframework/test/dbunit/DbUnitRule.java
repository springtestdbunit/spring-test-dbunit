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
import org.springframework.test.dbunit.annotation.DatabaseSetup;
import org.springframework.test.dbunit.annotation.DatabaseTearDown;
import org.springframework.test.dbunit.annotation.ExpectedDatabase;
import org.springframework.test.dbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.springframework.test.dbunit.dataset.DataSetLoader;
import org.springframework.test.dbunit.dataset.FlatXmlDataSetLoader;
import org.springframework.util.ReflectionUtils;

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
 * 
 * @author Phillip Webb
 */
public class DbUnitRule implements MethodRule {

	private static DbUnitRunner runner = new DbUnitRunner();

	private static Map<Class<?>, TestClassFields> fields = new HashMap<Class<?>, DbUnitRule.TestClassFields>();

	private IDatabaseConnection connection;

	private DataSetLoader dataSetLoader;

	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		DbUnitTestContext context = new DbUnitTestContextAdapter(method, target);
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

		public DbUnitTestContextAdapter(FrameworkMethod method, Object target) {
			this.method = method;
			this.target = target;
		}

		private boolean hasField(Class<?> type) {
			return getField(type) != null;
		}

		private <T> T getField(Class<T> type) {
			return getTestClassFields(getTestClass()).get(type, target);
		}

		public IDatabaseConnection getConnection() {
			if (connection == null) {
				if (hasField(IDatabaseConnection.class)) {
					connection = getField(IDatabaseConnection.class);
				} else if (hasField(DataSource.class)) {
					connection = DatabaseDataSourceConnectionFactoryBean.newConnection(getField(DataSource.class));
				} else {
					throw new IllegalStateException(
							"Unable to locate database connection for DbUnitRule.  Ensure that a DataSource or IDatabaseConnection "
									+ "is available as a private member of your test");
				}
			}
			return connection;
		}

		public DataSetLoader getDataSetLoader() {
			if (dataSetLoader == null) {
				if (hasField(DataSetLoader.class)) {
					dataSetLoader = getField(DataSetLoader.class);
				} else {
					dataSetLoader = new FlatXmlDataSetLoader();
				}
			}
			return dataSetLoader;
		}

		public Class<?> getTestClass() {
			return method.getMethod().getDeclaringClass();
		}

		public Method getTestMethod() {
			return method.getMethod();
		}
	}

	private class DbUnitStatement extends Statement {

		private Statement nextStatement;
		private DbUnitTestContext testContext;

		public DbUnitStatement(DbUnitTestContext testContext, Statement nextStatement) {
			this.testContext = testContext;
			this.nextStatement = nextStatement;
		}

		@Override
		public void evaluate() throws Throwable {
			runner.beforeTestMethod(testContext);
			nextStatement.evaluate();
			runner.afterTestMethod(testContext);
		}
	}

	private static class TestClassFields {

		private Map<Class<?>, Set<Field>> fieldMap = new HashMap<Class<?>, Set<Field>>();

		private Class<?> testClass;

		public TestClassFields(Class<?> testClass) {
			this.testClass = testClass;
		}

		private Set<Field> getFields(final Class<?> type) {
			if (fieldMap.containsKey(type)) {
				return fieldMap.get(type);
			}
			final Set<Field> fields = new HashSet<Field>();
			ReflectionUtils.doWithFields(testClass, new ReflectionUtils.FieldCallback() {
				public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
					if (type.isAssignableFrom(field.getType())) {
						field.setAccessible(true);
						fields.add(field);
					}
				}
			});
			fieldMap.put(type, fields);
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
					throw new IllegalStateException("Unable to read field of type " + type + " from " + testClass, e);
				}
			}
			throw new IllegalStateException("Unable to read a single value from multiple fields of type " + type
					+ " from " + testClass);
		}
	}
}
