/*
 * Copyright 2002-2013 the original author or authors
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedCompositeDatabase;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertion;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.github.springtestdbunit.dataset.CompositeDataSetLoader;
import com.github.springtestdbunit.dataset.DataSetLoader;

/**
 * Internal delegate class used to run tests with support for {@link DatabaseSetup &#064;DatabaseSetup},
 * {@link DatabaseTearDown &#064;DatabaseTearDown}, {@link ExpectedDatabase &#064;ExpectedDatabase} and
 * {@link ExpectedCompositeDatabase &#064;ExpectedCompositeDatabase} annotations.
 * 
 * @author Phillip Webb
 * @author Mario Zagar
 * @author Sunitha Rajarathnam
 * @author Vseslav Suvorov
 */
class DbUnitRunner {

	private static final Log logger = LogFactory.getLog(DbUnitTestExecutionListener.class);

	/**
	 * Called before a test method is executed to perform any database setup.
	 * @param testContext The test context
	 * @throws Exception
	 */
	public void beforeTestMethod(DbUnitTestContext testContext) throws Exception {
		Collection<DatabaseSetup> annotations = getAnnotations(testContext, DatabaseSetup.class);
		setupOrTeardown(testContext, true, AnnotationAttributes.get(annotations));
	}

	/**
	 * Called after a test method is executed to perform any database teardown and to check expected results.
	 * @param testContext The test context
	 * @throws Exception
	 */
	public void afterTestMethod(DbUnitTestContext testContext) throws Exception {
		try {
			verifyExpected(testContext, new ExpectedDatabaseAccessor());
			verifyExpected(testContext, new ExpectedCompositeDatabaseAccessor());
			Collection<DatabaseTearDown> annotations = getAnnotations(testContext, DatabaseTearDown.class);
			try {
				setupOrTeardown(testContext, false, AnnotationAttributes.get(annotations));
			} catch (RuntimeException e) {
				if (testContext.getTestException() == null) {
					throw e;
				}
				if (logger.isWarnEnabled()) {
					logger.warn("Unable to throw database cleanup exception due to existing test error", e);
				}
			}
		} finally {
			testContext.getConnection().close();
		}
	}

	private static <T extends Annotation> Collection<T> getAnnotations(DbUnitTestContext testContext,
			Class<T> annotationType) {
		List<T> annotations = new ArrayList<T>();
		addAnnotationToList(annotations, AnnotationUtils.findAnnotation(testContext.getTestClass(), annotationType));
		addAnnotationToList(annotations, AnnotationUtils.findAnnotation(testContext.getTestMethod(), annotationType));
		return annotations;
	}

	private static <T extends Annotation> void addAnnotationToList(List<T> annotations, T annotation) {
		if (annotation != null) {
			annotations.add(annotation);
		}
	}
	
	private <T extends Annotation> void verifyExpected(DbUnitTestContext testContext,
			ExpectedAnnotationAccessor<T> expectedAccessor) throws Exception {
		if (testContext.getTestException() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping @DatabaseTest expectation due to test exception "
						+ testContext.getTestException().getClass());
			}
			return;
		}
		IDatabaseConnection connection = testContext.getConnection();
		for (T annotation : expectedAccessor.getAnnotations(testContext)) {
			String query = expectedAccessor.query(annotation);
			String table = expectedAccessor.table(annotation);
			IDataSet expectedDataSet = expectedAccessor.loadDataset(testContext, annotation);
			if (expectedDataSet != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Veriftying @DatabaseTest expectation using "
							+ expectedAccessor.valueAsString(annotation));
				}
				DatabaseAssertion assertion = expectedAccessor.assertionMode(annotation).getDatabaseAssertion();
				if (StringUtils.hasLength(query)) {
					Assert.hasLength(table, "The table name must be specified when using a SQL query");
					ITable expectedTable = expectedDataSet.getTable(table);
					ITable actualTable = connection.createQueryTable(table, query);
					assertion.assertEquals(expectedTable, actualTable);
				} else if (StringUtils.hasLength(table)) {
					ITable actualTable = connection.createTable(table);
					ITable expectedTable = expectedDataSet.getTable(table);
					assertion.assertEquals(expectedTable, actualTable);
				} else {
					IDataSet actualDataSet = connection.createDataSet();
					assertion.assertEquals(expectedDataSet, actualDataSet);
				}
			}
		}
	}

	private static IDataSet loadDataset(DbUnitTestContext testContext, String dataSetLocation) throws Exception {
		DataSetLoader dataSetLoader = testContext.getDataSetLoader();
		if (StringUtils.hasLength(dataSetLocation)) {
			IDataSet dataSet = dataSetLoader.loadDataSet(testContext.getTestClass(), dataSetLocation);
			Assert.notNull(dataSet,
					"Unable to load dataset from \"" + dataSetLocation + "\" using " + dataSetLoader.getClass());
			return dataSet;
		}
		return null;
	}

	private static IDataSet loadDataset(DbUnitTestContext testContext, String[] dataSetLocations,
			boolean combine, boolean caseSensitiveTableNames) throws Exception {
		DataSetLoader dataSetLoader = testContext.getDataSetLoader();
		Assert.isInstanceOf(CompositeDataSetLoader.class, dataSetLoader,
				"Invalid data set loader is specified: ");
		CompositeDataSetLoader compositeDataSetLoader = (CompositeDataSetLoader) dataSetLoader;
		if (dataSetLocations.length > 0) {
			IDataSet dataSet = compositeDataSetLoader.loadDataSet(testContext.getTestClass(), dataSetLocations,
					combine, caseSensitiveTableNames);
			Assert.notNull(dataSet,
					"Unable to load dataset from \"" + Arrays.toString(dataSetLocations)
							+ "\" using " + dataSetLoader.getClass());
			return dataSet;
		}
		return null;
	}

	private void setupOrTeardown(DbUnitTestContext testContext, boolean isSetup,
			Collection<AnnotationAttributes> annotations) throws Exception {
		IDatabaseConnection connection = testContext.getConnection();
		DatabaseOperation lastOperation = null;
		for (AnnotationAttributes annotation : annotations) {
			for (String dataSetLocation : annotation.getValue()) {
				DatabaseOperation operation = annotation.getType();
				org.dbunit.operation.DatabaseOperation dbUnitDatabaseOperation = getDbUnitDatabaseOperation(
						testContext, operation, lastOperation);
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

	private org.dbunit.operation.DatabaseOperation getDbUnitDatabaseOperation(DbUnitTestContext testContext,
			DatabaseOperation operation, DatabaseOperation lastOperation) {
		if ((operation == DatabaseOperation.CLEAN_INSERT) && (lastOperation == DatabaseOperation.CLEAN_INSERT)) {
			operation = DatabaseOperation.INSERT;
		}
		org.dbunit.operation.DatabaseOperation databaseOperation = testContext.getDatbaseOperationLookup().get(
				operation);
		Assert.state(databaseOperation != null, "The database operation " + operation + " is not supported");
		return databaseOperation;
	}

	private static class AnnotationAttributes {

		private DatabaseOperation type;

		private String[] value;

		public AnnotationAttributes(Annotation annotation) {
			Assert.state((annotation instanceof DatabaseSetup) || (annotation instanceof DatabaseTearDown),
					"Only DatabaseSetup and DatabaseTearDown annotations are supported");
			Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
			this.type = (DatabaseOperation) attributes.get("type");
			this.value = (String[]) attributes.get("value");
		}

		public DatabaseOperation getType() {
			return this.type;
		}

		public String[] getValue() {
			return this.value;
		}

		public static <T extends Annotation> Collection<AnnotationAttributes> get(Collection<T> annotations) {
			List<AnnotationAttributes> annotationAttributes = new ArrayList<AnnotationAttributes>();
			for (T annotation : annotations) {
				annotationAttributes.add(new AnnotationAttributes(annotation));
			}
			return annotationAttributes;
		}
	}
	
	private interface ExpectedAnnotationAccessor<T extends Annotation> {
		
		Collection<T> getAnnotations(DbUnitTestContext testContext);

		String valueAsString(T annotation);
		
		IDataSet loadDataset(DbUnitTestContext testContext, T annotation) throws Exception;
		
		DatabaseAssertionMode assertionMode(T annotation);
		
		String table(T annotation);
		
		String query(T annotation);
	}
	
	private static class ExpectedDatabaseAccessor implements ExpectedAnnotationAccessor<ExpectedDatabase> {

		public Collection<ExpectedDatabase> getAnnotations(DbUnitTestContext testContext) {
			return DbUnitRunner.getAnnotations(testContext, ExpectedDatabase.class);
		}

		public String valueAsString(ExpectedDatabase annotation) {
			return annotation.value();
		}

		public IDataSet loadDataset(DbUnitTestContext testContext, ExpectedDatabase annotation) throws Exception {
			return DbUnitRunner.loadDataset(testContext, annotation.value());
		}

		public DatabaseAssertionMode assertionMode(ExpectedDatabase annotation) {
			return annotation.assertionMode();
		}

		public String table(ExpectedDatabase annotation) {
			return annotation.table();
		}

		public String query(ExpectedDatabase annotation) {
			return annotation.query();
		}
	}
	
	private static class ExpectedCompositeDatabaseAccessor implements
			ExpectedAnnotationAccessor<ExpectedCompositeDatabase> {

		public Collection<ExpectedCompositeDatabase> getAnnotations(DbUnitTestContext testContext) {
			return DbUnitRunner.getAnnotations(testContext, ExpectedCompositeDatabase.class);
		}

		public String valueAsString(ExpectedCompositeDatabase annotation) {
			return Arrays.toString(annotation.value());
		}

		public IDataSet loadDataset(DbUnitTestContext testContext, ExpectedCompositeDatabase annotation)
				throws Exception {
			return DbUnitRunner.loadDataset(testContext, annotation.value(), annotation.combine(),
					annotation.caseSensitiveTableNames());
		}

		public DatabaseAssertionMode assertionMode(ExpectedCompositeDatabase annotation) {
			return annotation.assertionMode();
		}

		public String table(ExpectedCompositeDatabase annotation) {
			return annotation.table();
		}

		public String query(ExpectedCompositeDatabase annotation) {
			return annotation.query();
		}
	}
}
