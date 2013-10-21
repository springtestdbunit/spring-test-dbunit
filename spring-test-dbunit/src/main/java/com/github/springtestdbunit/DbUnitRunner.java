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

import java.lang.annotation.Annotation;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertion;
import com.github.springtestdbunit.dataset.DataSetLoader;

/**
 * Internal delegate class used to run tests with support for {@link com.github.springtestdbunit.annotation.DatabaseSetup &#064;DatabaseSetup},
 * {@link com.github.springtestdbunit.annotation.DatabaseTearDown &#064;DatabaseTearDown} and {@link com.github.springtestdbunit.annotation.ExpectedDatabase &#064;ExpectedDatabase} annotations.
 * 
 * @author Phillip Webb
 * @author Mario Zagar
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
			IDataSet expectedDataSet = loadDataset(testContext, annotation.value(), annotation.replaceSequenceIds());
			if (expectedDataSet != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Veriftying @DatabaseTest expectation using " + annotation.value());
				}
				DatabaseAssertion assertion = annotation.assertionMode().getDatabaseAssertion();
				assertion.assertEquals(expectedDataSet, actualDataSet);
			}
		}
	}

	private IDataSet loadDataset(DbUnitTestContext testContext, String dataSetLocation, boolean replaceSequenceIds)
			throws Exception {
		DataSetLoader dataSetLoader = testContext.getDataSetLoader();
		if (StringUtils.hasLength(dataSetLocation)) {
			IDataSet dataSet = dataSetLoader.loadDataSet(testContext.getTestClass(), dataSetLocation);
			Assert.notNull(dataSet,
					"Unable to load dataset from \"" + dataSetLocation + "\" using " + dataSetLoader.getClass());

			if (replaceSequenceIds) {
				replaceSequences(dataSet, testContext.getConnection());
			}

			return dataSet;
		}
		return null;
	}

	private IDataSet loadDataset(DbUnitTestContext testContext, String dataSetLocation) throws Exception {
		return loadDataset(testContext, dataSetLocation, false);
	}

	/**
	 * Replaces the sequences names with the sequences values in a DataSet containing the expression "${...}" as a value
	 * name.
	 * @see com.github.springtestdbunit.annotation.ExpectedDatabase#replaceSequenceIds()
	 * 
	 * @param dataSet the dataSet in which we will replace the sequences values.
	 * @param databaseConnection the connection to the database needed to query the sequences values.
	 * @throws Exception
	 */
	private void replaceSequences(IDataSet dataSet, IDatabaseConnection databaseConnection) throws Exception {
		ITableIterator iter = dataSet.iterator();

		// We iterate on the table names
		while (iter.next()) {
			ITable table = iter.getTable();
			Column[] cols = table.getTableMetaData().getColumns();

			// We use this Map the count the number of times a sequence name is used in order to retroactively assign
			// an ID based on the sequence value.
			Map<String, ArrayList<TableValue>> sequences = new HashMap<String, ArrayList<TableValue>>();

			// We iterate on the lines and columns
			for (int i = 0; i < table.getRowCount(); i++) {
				for (Column column : cols) {

					// We verify that the value is not null
					Object o = table.getValue(i, column.getColumnName());
					if (o != null) {

						// We look for the expression "${...}" in the value
						String value = o.toString();
						if (value.startsWith("${") && value.endsWith("}")) {

							// We retrieve the sequence name
							String key = value.substring(2, value.length() - 1);

							// if the map does not contain the list corresponding to the key we create it
							if (sequences.containsKey(key) == false) {
								sequences.put(key, new ArrayList<TableValue>());
							}

							// we stock the data of the element we just found
							List<TableValue> liste = sequences.get(key);
							TableValue e = new TableValue(i, column.getColumnName());
							liste.add(e);
						}
					}
				}
			}

			// we iterate the sequence names and we replace them in the dataset with the index
			// that it should have according to number of times we found it in the dataset
			for (String seq : sequences.keySet()) {
				List<TableValue> liste = sequences.get(seq);
				long nextVal = getNextSequenceValue(seq, databaseConnection);
				long idValue = nextVal - liste.size();

				for (TableValue e : liste) {
					DefaultTable dt = (DefaultTable) table;
					dt.setValue(e.getRow(), e.getColumnName(), idValue);
					idValue++;
				}
			}
		}
	}

	/**
	 * Retrieves the next value in a SQL Sequence.
	 * 
	 * @param sequenceName
	 * @param databaseConnection
	 * @return
	 * @throws Exception
	 */
	private long getNextSequenceValue(String sequenceName, IDatabaseConnection databaseConnection) throws Exception {
		Statement st = databaseConnection.getConnection().createStatement();
		ResultSet rs = st.executeQuery("SELECT " + sequenceName + ".nextval FROM dual");
		rs.next();
		return rs.getLong(1);
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
		Assert.state(databaseOperation != null, "The databse operation " + operation + " is not supported");
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

	/**
	 * Utility class to stock the row and columnName of the elements in the DataSet in which there is an Oracle Sequence
	 * to replace.
	 * 
	 * @author cachavezley
	 */
	private class TableValue {
		private final int row;
		private final String columnName;

		private TableValue(int row, String columnName) {
			this.row = row;
			this.columnName = columnName;
		}

		private int getRow() {
			return this.row;
		}

		private String getColumnName() {
			return this.columnName;
		}

		@Override
		public String toString() {
			return "TableValue [row=" + this.row + ", columnName=" + this.columnName + "]";
		}
	}
}
