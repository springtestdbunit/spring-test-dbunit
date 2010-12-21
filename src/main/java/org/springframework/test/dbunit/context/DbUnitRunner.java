package org.springframework.test.dbunit.context;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.dbunit.DataSetLoader;
import org.springframework.test.dbunit.annotation.DatabaseOperation;
import org.springframework.test.dbunit.annotation.DatabaseSetup;
import org.springframework.test.dbunit.annotation.DatabaseTearDown;
import org.springframework.test.dbunit.annotation.ExpectedDatabase;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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

	public void beforeTestMethod(DbUnitTestContext testContext) throws Exception {
		Collection<DatabaseSetup> annotations = getAnnotations(testContext, DatabaseSetup.class);
		setupOrTeardown(testContext, true, AnnotationAttributes.get(annotations));
	}

	public void afterTestMethod(DbUnitTestContext testContext) throws Exception {
		verifyExpected(testContext, getAnnotations(testContext, ExpectedDatabase.class));
		Collection<DatabaseTearDown> annotations = getAnnotations(testContext, DatabaseTearDown.class);
		setupOrTeardown(testContext, false, AnnotationAttributes.get(annotations));
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

	private void verifyExpected(DbUnitTestContext testContext, Collection<ExpectedDatabase> annotations) throws Exception {
		IDatabaseConnection connection = testContext.getConnection();
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

	private IDataSet loadDataset(DbUnitTestContext testContext, String dataSetLocation) throws Exception {
		DataSetLoader dataSetLoader = testContext.getDataSetLoader();
		if (StringUtils.hasLength(dataSetLocation)) {
			IDataSet dataSet = dataSetLoader.loadDataSet(testContext.getTestClass(), dataSetLocation);
			Assert.notNull(dataSet, "Unable to load dataset from \"" + dataSetLocation + "\" using "
					+ dataSetLoader.getClass());
			return dataSet;
		}
		return null;
	}

	private void setupOrTeardown(DbUnitTestContext testContext, boolean isSetup, Collection<AnnotationAttributes> annotations)
			throws Exception {
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
