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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.dbunit.annotation.DatabaseOperation;
import org.springframework.test.dbunit.annotation.DatabaseSetup;
import org.springframework.test.dbunit.annotation.DatabaseTearDown;
import org.springframework.test.dbunit.annotation.ExpectedDatabase;
import org.springframework.test.dbunit.assertion.DatabaseAssertion;
import org.springframework.test.dbunit.assertion.DatabaseAssertionFactory;
import org.springframework.test.dbunit.dataset.DataSetLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Internal delegate class used to run tests with support for {@link DatabaseSetup &#064;DatabaseSetup},
 * {@link DatabaseTearDown &#064;DatabaseTearDown} and {@link ExpectedDatabase &#064;ExpectedDatabase} annotations.
 * 
 * @author Phillip Webb
 */
class DbUnitRunner {

	private static final Log logger = LogFactory.getLog(DbUnitTestExecutionListener.class);

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
			verifyExpected(testContext, getAnnotations(testContext, ExpectedDatabase.class));
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

	private <T extends Annotation> Collection<T> getAnnotations(DbUnitTestContext testContext, Class<T> annotationType) {
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

	private void verifyExpected(DbUnitTestContext testContext, Collection<ExpectedDatabase> annotations)
			throws Exception {
		if (testContext.getTestException() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping @DatabaseTest expectation due to test exception "
						+ testContext.getTestException().getClass());
			}
			return;
		}
		IDatabaseConnection connection = testContext.getConnection();
		IDataSet actualDataSet = connection.createDataSet();
		for (ExpectedDatabase annotation : annotations) {
			IDataSet expectedDataSet = loadDataset(testContext, annotation.value());
			if (expectedDataSet != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Veriftying @DatabaseTest expectation using " + annotation.value());
				}
				
				DatabaseAssertion assertion = DatabaseAssertionFactory.createDatabaseAssertion(annotation.assertionMode());
				assertion.assertEquals(expectedDataSet, actualDataSet);
			}
		}
	}

	private IDataSet loadDataset(DbUnitTestContext testContext, String dataSetLocation) throws Exception {
		DataSetLoader dataSetLoader = testContext.getDataSetLoader();
		if (StringUtils.hasLength(dataSetLocation)) {
			IDataSet dataSet = dataSetLoader.loadDataSet(testContext.getTestClass(), dataSetLocation);
			Assert.notNull(dataSet,
					"Unable to load dataset from \"" + dataSetLocation + "\" using " + dataSetLoader.getClass());
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
