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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.Assertion;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.springframework.core.Conventions;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.dbunit.DataSetLoader;
import org.springframework.test.dbunit.FlatXmlDataSetLoader;
import org.springframework.test.dbunit.annotation.DatabaseOperation;
import org.springframework.test.dbunit.annotation.DatabaseSetup;
import org.springframework.test.dbunit.annotation.DatabaseTearDown;
import org.springframework.test.dbunit.annotation.DbUnitConfiguration;
import org.springframework.test.dbunit.annotation.ExpectedDatabase;
import org.springframework.test.dbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <code>TestExecutionListener</code> which provides support for {@link DatabaseSetup &#064;DatabaseSetup},
 * {@link DatabaseTearDown &#064;DatabaseTearDown} and {@link ExpectedDatabase &#064;ExpectedDatabase} annotations.
 * <p>
 * A bean named "<tt>dbUnitDatabaseConnection</tt>" or "<tt>dataSource</tt>" is expected in the
 * <tt>ApplicationContext</tt> associated with the test. This bean can contain either a {@link IDatabaseConnection} or a
 * {@link DataSource}. A custom bean name can also be specified using the
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

	private static Map<DatabaseOperation, org.dbunit.operation.DatabaseOperation> OPERATION_LOOKUP;
	static {
		OPERATION_LOOKUP = new HashMap<DatabaseOperation, org.dbunit.operation.DatabaseOperation>();
		OPERATION_LOOKUP.put(DatabaseOperation.UPDATE, org.dbunit.operation.DatabaseOperation.UPDATE);
		OPERATION_LOOKUP.put(DatabaseOperation.INSERT, org.dbunit.operation.DatabaseOperation.INSERT);
		OPERATION_LOOKUP.put(DatabaseOperation.REFRESH, org.dbunit.operation.DatabaseOperation.REFRESH);
		OPERATION_LOOKUP.put(DatabaseOperation.DELETE, org.dbunit.operation.DatabaseOperation.DELETE);
		OPERATION_LOOKUP.put(DatabaseOperation.DELETE_ALL, org.dbunit.operation.DatabaseOperation.DELETE_ALL);
		OPERATION_LOOKUP.put(DatabaseOperation.TRUNCATE_TABLE, org.dbunit.operation.DatabaseOperation.TRUNCATE_TABLE);
		OPERATION_LOOKUP.put(DatabaseOperation.CLEAN_INSERT, org.dbunit.operation.DatabaseOperation.CLEAN_INSERT);
	}

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
			databaseConnection = createDatabaseDataSourceConnection((DataSource) databaseConnection);
		}
		Assert.isInstanceOf(IDatabaseConnection.class, databaseConnection);
		testContext.setAttribute(CONNECTION_ATTRIBUTE, databaseConnection);
	}

	private DatabaseDataSourceConnection createDatabaseDataSourceConnection(DataSource dataSource) throws Exception {
		DatabaseDataSourceConnectionFactoryBean factoryBean = new DatabaseDataSourceConnectionFactoryBean();
		factoryBean.setDataSource(dataSource);
		return factoryBean.getObject();
	}

	private void prepareDataSetLoader(TestContext testContext, Class<? extends DataSetLoader> dataSetLoaderClass)
			throws Exception {
		try {
			testContext.setAttribute(DATA_SET_LOADER_ATTRIBUTE, dataSetLoaderClass.newInstance());
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to create data set loader instance for " + dataSetLoaderClass, e);
		}
	}

	public void beforeTestMethod(TestContext testContext) throws Exception {
		Collection<DatabaseSetup> annotations = getAnnotations(testContext, DatabaseSetup.class);
		setupOrTeardown(testContext, true, AnnotationAttributes.get(annotations));
	}

	public void afterTestMethod(TestContext testContext) throws Exception {
		verifyExpected(testContext, getAnnotations(testContext, ExpectedDatabase.class));
		Collection<DatabaseTearDown> annotations = getAnnotations(testContext, DatabaseTearDown.class);
		setupOrTeardown(testContext, false, AnnotationAttributes.get(annotations));
	}

	private <T extends Annotation> Collection<T> getAnnotations(TestContext testContext, Class<T> annotationType) {
		List<T> annotations = new ArrayList<T>();
		addAnnotationToList(annotations, AnnotationUtils.findAnnotation(testContext.getTestClass(), annotationType));
		addAnnotationToList(annotations, AnnotationUtils.findAnnotation(testContext.getTestMethod(), annotationType));
		return annotations;
	}

	private <T extends Annotation> void addAnnotationToList(List<T> annotations, T annotation) {
		if (annotation != null) {
			annotations.add(annotation);
		}
	}

	private void verifyExpected(TestContext testContext, Collection<ExpectedDatabase> annotations) throws Exception {
		IDatabaseConnection connection = getConnection(testContext);
		IDataSet actualDataSet = connection.createDataSet();
		for (ExpectedDatabase annotation : annotations) {
			IDataSet expectedDataSet = loadDataset(testContext, annotation.value());
			if (expectedDataSet != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Veriftying @DatabaseTest expectation using " + annotation.value());
				}
				Assertion.assertEquals(expectedDataSet, actualDataSet);
			}
		}
	}

	private IDataSet loadDataset(TestContext testContext, String dataSetLocation) throws Exception {
		DataSetLoader dataSetLoader = getDataSetLoader(testContext);
		if (StringUtils.hasLength(dataSetLocation)) {
			IDataSet dataSet = dataSetLoader.loadDataSet(testContext, dataSetLocation);
			Assert.notNull(dataSet, "Unable to load dataset from \"" + dataSetLocation + "\" using "
					+ dataSetLoader.getClass());
			return dataSet;
		}
		return null;
	}

	private void setupOrTeardown(TestContext testContext, boolean isSetup, Collection<AnnotationAttributes> annotations)
			throws Exception {
		IDatabaseConnection connection = getConnection(testContext);
		DatabaseOperation lastOperation = null;
		for (AnnotationAttributes annotation : annotations) {
			for (String dataSetLocation : annotation.getValue()) {
				DatabaseOperation operation = annotation.getType();
				org.dbunit.operation.DatabaseOperation dbUnitDatabaseOperation = getDbUnitDatabaseOperation(operation,
						lastOperation);
				IDataSet dataSet = loadDataset(testContext, dataSetLocation);
				if (dataSet != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Executing " + (isSetup ? "Setup" : "Teardown") + " of @DatabaseTest using "
								+ operation + " on " + dataSetLocation);
					}
					dbUnitDatabaseOperation.execute(connection, dataSet);
					lastOperation = operation;
				}
			}
		}
	}

	private IDatabaseConnection getConnection(TestContext testContext) {
		return (IDatabaseConnection) testContext.getAttribute(CONNECTION_ATTRIBUTE);
	}

	private DataSetLoader getDataSetLoader(TestContext testContext) {
		return (DataSetLoader) testContext.getAttribute(DATA_SET_LOADER_ATTRIBUTE);
	}

	private org.dbunit.operation.DatabaseOperation getDbUnitDatabaseOperation(DatabaseOperation operation,
			DatabaseOperation lastOperation) {
		if (operation == DatabaseOperation.CLEAN_INSERT && lastOperation == DatabaseOperation.CLEAN_INSERT) {
			operation = DatabaseOperation.INSERT;
		}
		org.dbunit.operation.DatabaseOperation databaseOperation = OPERATION_LOOKUP.get(operation);
		Assert.state(databaseOperation != null, "The databse operation " + operation + " is not supported");
		return databaseOperation;
	}

	private static class AnnotationAttributes {

		private DatabaseOperation type;
		private String[] value;

		public AnnotationAttributes(Annotation annotation) {
			Assert.state(annotation instanceof DatabaseSetup || annotation instanceof DatabaseTearDown,
					"Only DatabaseSetup and DatabaseTearDown annotations are supported");
			Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
			this.type = (DatabaseOperation) attributes.get("type");
			this.value = (String[]) attributes.get("value");
		}

		public DatabaseOperation getType() {
			return type;
		}

		public String[] getValue() {
			return value;
		}

		public static <T extends Annotation> Collection<AnnotationAttributes> get(Collection<T> annotations) {
			List<AnnotationAttributes> annotationAttributes = new ArrayList<AnnotationAttributes>();
			for (T annotation : annotations) {
				annotationAttributes.add(new AnnotationAttributes(annotation));
			}
			return annotationAttributes;
		}
	}
}
