/*
 * Copyright 2002-2015 the original author or authors
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.DatabaseTearDowns;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.annotation.ExpectedDatabases;
import com.github.springtestdbunit.assertion.DatabaseAssertion;
import com.github.springtestdbunit.dataset.DataSetLoader;
import com.github.springtestdbunit.dataset.DataSetModifier;

/**
 * Internal delegate class used to run tests with support for {@link DatabaseSetup &#064;DatabaseSetup},
 * {@link DatabaseTearDown &#064;DatabaseTearDown} and {@link ExpectedDatabase &#064;ExpectedDatabase} annotations.
 *
 * @author Phillip Webb
 * @author Mario Zagar
 * @author Sunitha Rajarathnam
 * @author Oleksii Lomako
 */
class DbUnitRunner {

	private static final Log logger = LogFactory.getLog(DbUnitTestExecutionListener.class);

	/**
	 * Called before a test method is executed to perform any database setup.
	 * @param testContext The test context
	 * @throws Exception
	 */
	public void beforeTestMethod(DbUnitTestContext testContext) throws Exception {
		Collection<DatabaseSetup> annotations = getAnnotations(testContext, DatabaseSetups.class, DatabaseSetup.class);
		setupOrTeardown(testContext, true, AnnotationAttributes.get(annotations));
	}

	/**
	 * Called after a test method is executed to perform any database teardown and to check expected results.
	 * @param testContext The test context
	 * @throws Exception
	 */
	public void afterTestMethod(DbUnitTestContext testContext) throws Exception {
		try {
			try {
				verifyExpected(testContext,
						getAnnotations(testContext, ExpectedDatabases.class, ExpectedDatabase.class));
			} finally {
				Collection<DatabaseTearDown> annotations = getAnnotations(testContext, DatabaseTearDowns.class,
						DatabaseTearDown.class);
				try {
					setupOrTeardown(testContext, false, AnnotationAttributes.get(annotations));
				} catch (RuntimeException ex) {
					if (testContext.getTestException() == null) {
						throw ex;
					}
					if (logger.isWarnEnabled()) {
						logger.warn("Unable to throw database cleanup exception due to existing test error", ex);
					}
				}
			}
		} finally {
			testContext.getConnections().closeAll();
		}
	}

	private <T extends Annotation> List<T> getAnnotations(DbUnitTestContext testContext,
			Class<? extends Annotation> containerType, Class<T> type) {
		List<T> annotations = new ArrayList<T>();
		addAnnotationToList(annotations, AnnotationUtils.findAnnotation(testContext.getTestClass(), type));
		addRepeatableAnnotationsToList(annotations,
				AnnotationUtils.findAnnotation(testContext.getTestClass(), containerType));
		addAnnotationToList(annotations, AnnotationUtils.findAnnotation(testContext.getTestMethod(), type));
		addRepeatableAnnotationsToList(annotations,
				AnnotationUtils.findAnnotation(testContext.getTestMethod(), containerType));
		return annotations;
	}

	private <T extends Annotation> void addAnnotationToList(List<T> annotations, T annotation) {
		if (annotation != null) {
			annotations.add(annotation);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> void addRepeatableAnnotationsToList(List<T> annotations,
			Annotation annotationContainer) {
		if (annotationContainer != null) {
			T[] value = (T[]) AnnotationUtils.getValue(annotationContainer);
			for (T annotation : value) {
				annotations.add(annotation);
			}
		}
	}

	private void verifyExpected(DbUnitTestContext testContext, List<ExpectedDatabase> annotations) throws Exception {
		if (testContext.getTestException() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping @DatabaseTest expectation due to test exception "
						+ testContext.getTestException().getClass());
			}
			return;
		}
		DatabaseConnections connections = testContext.getConnections();
		DataSetModifier modifier = getModifier(testContext, annotations);
		for (int i = annotations.size() - 1; i >= 0; i--) {
			ExpectedDatabase annotation = annotations.get(i);
			String query = annotation.query();
			String table = annotation.table();
			IDataSet expectedDataSet = loadDataset(testContext, annotation.value(), modifier);
			IDatabaseConnection connection = connections.get(annotation.connection());
			if (expectedDataSet != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Veriftying @DatabaseTest expectation using " + annotation.value());
				}
				DatabaseAssertion assertion = annotation.assertionMode().getDatabaseAssertion();
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
			if (annotation.override()) {
				// No need to test any more
				return;
			}
		}

	}

	private DataSetModifier getModifier(DbUnitTestContext testContext, List<ExpectedDatabase> annotations) {
		DataSetModifiers modifiers = new DataSetModifiers();
		for (ExpectedDatabase annotation : annotations) {
			for (Class<? extends DataSetModifier> modifierClass : annotation.modifiers()) {
				modifiers.add(testContext.getTestInstance(), modifierClass);
			}
		}
		return modifiers;
	}

	private void setupOrTeardown(DbUnitTestContext testContext, boolean isSetup,
			Collection<AnnotationAttributes> annotations) throws Exception {
		DatabaseConnections connections = testContext.getConnections();
		for (AnnotationAttributes annotation : annotations) {
			List<IDataSet> datasets = loadDataSets(testContext, annotation);
			DatabaseOperation operation = annotation.getType();
			org.dbunit.operation.DatabaseOperation dbUnitOperation = getDbUnitDatabaseOperation(testContext, operation);
			if (!datasets.isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Executing " + (isSetup ? "Setup" : "Teardown") + " of @DatabaseTest using "
							+ operation + " on " + datasets.toString());
				}
				IDatabaseConnection connection = connections.get(annotation.getConnection());
				IDataSet dataSet = new CompositeDataSet(datasets.toArray(new IDataSet[datasets.size()]));
				dbUnitOperation.execute(connection, dataSet);
			}
		}
	}

	private List<IDataSet> loadDataSets(DbUnitTestContext testContext, AnnotationAttributes annotation)
			throws Exception {
		List<IDataSet> datasets = new ArrayList<IDataSet>();
		for (String dataSetLocation : annotation.getValue()) {
			datasets.add(loadDataset(testContext, dataSetLocation, DataSetModifier.NONE));
		}
		return datasets;
	}

	private IDataSet loadDataset(DbUnitTestContext testContext, String dataSetLocation, DataSetModifier modifier)
			throws Exception {
		DataSetLoader dataSetLoader = testContext.getDataSetLoader();
		if (StringUtils.hasLength(dataSetLocation)) {
			IDataSet dataSet = dataSetLoader.loadDataSet(testContext.getTestClass(), dataSetLocation);
			dataSet = modifier.modify(dataSet);
			Assert.notNull(dataSet,
					"Unable to load dataset from \"" + dataSetLocation + "\" using " + dataSetLoader.getClass());
			return dataSet;
		}
		return null;
	}

	private org.dbunit.operation.DatabaseOperation getDbUnitDatabaseOperation(DbUnitTestContext testContext,
			DatabaseOperation operation) {
		org.dbunit.operation.DatabaseOperation databaseOperation = testContext.getDatbaseOperationLookup().get(
				operation);
		Assert.state(databaseOperation != null, "The database operation " + operation + " is not supported");
		return databaseOperation;
	}

	private static class AnnotationAttributes {

		private final DatabaseOperation type;

		private final String[] value;

		private final String connection;

		public AnnotationAttributes(Annotation annotation) {
			Assert.state((annotation instanceof DatabaseSetup) || (annotation instanceof DatabaseTearDown),
					"Only DatabaseSetup and DatabaseTearDown annotations are supported");
			Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
			this.type = (DatabaseOperation) attributes.get("type");
			this.value = (String[]) attributes.get("value");
			this.connection = (String) attributes.get("connection");
		}

		public DatabaseOperation getType() {
			return this.type;
		}

		public String[] getValue() {
			return this.value;
		}

		public String getConnection() {
			return this.connection;
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
