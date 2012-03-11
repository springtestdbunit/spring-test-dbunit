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
package org.github.springtestdbunit;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.IDatabaseConnection;
import org.springframework.core.Conventions;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.dbunit.annotation.DatabaseSetup;
import org.springframework.test.dbunit.annotation.DatabaseTearDown;
import org.springframework.test.dbunit.annotation.DbUnitConfiguration;
import org.springframework.test.dbunit.annotation.ExpectedDatabase;
import org.springframework.test.dbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.springframework.test.dbunit.dataset.DataSetLoader;
import org.springframework.test.dbunit.dataset.FlatXmlDataSetLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <code>TestExecutionListener</code> which provides support for {@link DatabaseSetup &#064;DatabaseSetup},
 * {@link DatabaseTearDown &#064;DatabaseTearDown} and {@link ExpectedDatabase &#064;ExpectedDatabase} annotations.
 * <p>
 * A bean named "<tt>dbUnitDatabaseConnection</tt>" or "<tt>dataSource</tt>" is expected in the
 * <tt>ApplicationContext</tt> associated with the test. This bean can contain either a {@link IDatabaseConnection} or a
 * {@link DataSource} . A custom bean name can also be specified using the
 * {@link DbUnitConfiguration#databaseConnection() &#064;DbUnitConfiguration} annotation.
 * <p>
 * Datasets are loaded using the {@link FlatXmlDataSetLoader} unless otherwise
 * {@link DbUnitConfiguration#dataSetLoader() configured}.
 * <p>
 * If you are running this listener in combination with the {@link TransactionalTestExecutionListener} then consider
 * using {@link TransactionDbUnitTestExecutionListener} instead.
 * 
 * @see TransactionDbUnitTestExecutionListener
 * 
 * @author Phillip Webb
 */
public class DbUnitTestExecutionListener extends AbstractTestExecutionListener {

	private static final Log logger = LogFactory.getLog(DbUnitTestExecutionListener.class);

	private static final String[] COMMON_DATABASE_CONNECTION_BEAN_NAMES = { "dbUnitDatabaseConnection", "dataSource" };

	protected static final String CONNECTION_ATTRIBUTE = Conventions.getQualifiedAttributeName(
			DbUnitTestExecutionListener.class, "connection");

	protected static final String DATA_SET_LOADER_ATTRIBUTE = Conventions.getQualifiedAttributeName(
			DbUnitTestExecutionListener.class, "dataSetLoader");

	private static DbUnitRunner runner = new DbUnitRunner();

	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug("Preparing test instance " + testContext.getTestClass() + " for DBUnit");
		}

		String databaseConnectionBeanName = null;
		Class<? extends DataSetLoader> dataSetLoaderClass = FlatXmlDataSetLoader.class;

		DbUnitConfiguration configuration = testContext.getTestClass().getAnnotation(DbUnitConfiguration.class);
		if (configuration != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Using @DbUnitConfiguration configuration");
			}
			databaseConnectionBeanName = configuration.databaseConnection();
			dataSetLoaderClass = configuration.dataSetLoader();
		}

		if (!StringUtils.hasLength(databaseConnectionBeanName)) {
			databaseConnectionBeanName = getDatabaseConnectionUsingCommonBeanNames(testContext);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("DBUnit tests will run using databaseConnection \"" + databaseConnectionBeanName
					+ "\", datasets will be loaded using " + dataSetLoaderClass);
		}
		prepareDatabaseConnection(testContext, databaseConnectionBeanName);
		prepareDataSetLoader(testContext, dataSetLoaderClass);
	}

	private String getDatabaseConnectionUsingCommonBeanNames(TestContext testContext) {
		for (String beanName : COMMON_DATABASE_CONNECTION_BEAN_NAMES) {
			if (testContext.getApplicationContext().containsBean(beanName)) {
				return beanName;
			}
		}
		throw new IllegalStateException(
				"Unable to find a DB Unit database connection, missing one the following beans: "
						+ Arrays.asList(COMMON_DATABASE_CONNECTION_BEAN_NAMES));
	}

	private void prepareDatabaseConnection(TestContext testContext, String databaseConnectionBeanName) throws Exception {
		Object databaseConnection = testContext.getApplicationContext().getBean(databaseConnectionBeanName);
		if (databaseConnection instanceof DataSource) {
			databaseConnection = DatabaseDataSourceConnectionFactoryBean.newConnection((DataSource) databaseConnection);
		}
		Assert.isInstanceOf(IDatabaseConnection.class, databaseConnection);
		testContext.setAttribute(CONNECTION_ATTRIBUTE, databaseConnection);
	}

	private void prepareDataSetLoader(TestContext testContext, Class<? extends DataSetLoader> dataSetLoaderClass)
			throws Exception {
		try {
			testContext.setAttribute(DATA_SET_LOADER_ATTRIBUTE, dataSetLoaderClass.newInstance());
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to create data set loader instance for " + dataSetLoaderClass, e);
		}
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		runner.beforeTestMethod(new DbUnitTestContextAdapter(testContext));
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		runner.afterTestMethod(new DbUnitTestContextAdapter(testContext));
	}

	private static class DbUnitTestContextAdapter implements DbUnitTestContext {

		private TestContext testContext;

		public DbUnitTestContextAdapter(TestContext testContext) {
			this.testContext = testContext;
		}

		public IDatabaseConnection getConnection() {
			return (IDatabaseConnection) this.testContext.getAttribute(CONNECTION_ATTRIBUTE);
		}

		public DataSetLoader getDataSetLoader() {
			return (DataSetLoader) this.testContext.getAttribute(DATA_SET_LOADER_ATTRIBUTE);
		}

		public Class<?> getTestClass() {
			return this.testContext.getTestClass();
		}

		public Method getTestMethod() {
			return this.testContext.getTestMethod();
		}

		public Throwable getTestException() {
			return this.testContext.getTestException();
		}
	}
}
