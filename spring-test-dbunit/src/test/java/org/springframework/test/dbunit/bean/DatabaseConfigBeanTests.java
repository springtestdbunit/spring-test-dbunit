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
package org.springframework.test.dbunit.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DefaultMetadataHandler;
import org.dbunit.database.IMetadataHandler;
import org.dbunit.database.IResultSetTableFactory;
import org.dbunit.database.statement.IStatementFactory;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.filter.IColumnFilter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Tests for {@link DatabaseConfigBean}.
 * 
 * @author Phillip Webb
 */
public class DatabaseConfigBeanTests {

	private static final Set<Class<?>> CLASS_COMPARE_ONLY;
	static {
		CLASS_COMPARE_ONLY = new HashSet<Class<?>>();
		CLASS_COMPARE_ONLY.add(DefaultMetadataHandler.class);
	}

	private DatabaseConfig defaultConfig = new DatabaseConfig();

	private DatabaseConfigBean configBean;
	private BeanWrapper configBeanWrapper;

	@Before
	public void setup() {
		this.configBean = new DatabaseConfigBean();
		this.configBeanWrapper = new BeanWrapperImpl(this.configBean);
	}

	@Test
	public void shouldAllowSetOfNonMandatoryFieldToNull() throws Exception {
		this.configBean.setPrimaryKeyFilter(null);
	}

	@Test
	public void shouldFailWhenSetingMandatoryFieldToNull() throws Exception {
		try {
			this.configBean.setDatatypeFactory(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("dataTypeFactory cannot be null", e.getMessage());
		}
	}

	@Test
	public void testStatementFactory() throws Exception {
		doTest("statementFactory", DatabaseConfig.PROPERTY_STATEMENT_FACTORY, mock(IStatementFactory.class));
	}

	@Test
	public void testResultsetTableFactory() {
		doTest("resultsetTableFactory", DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY,
				mock(IResultSetTableFactory.class));
	}

	@Test
	public void testDatatypeFactory() {
		doTest("datatypeFactory", DatabaseConfig.PROPERTY_DATATYPE_FACTORY, mock(IDataTypeFactory.class));
	}

	@Test
	public void testEscapePattern() {
		doTest("escapePattern", DatabaseConfig.PROPERTY_ESCAPE_PATTERN, "test");
	}

	@Test
	public void testTableType() {
		doTest("tableType", DatabaseConfig.PROPERTY_TABLE_TYPE, new String[] { "test" });
	}

	@Test
	public void testPrimaryKeyFilter() {
		doTest("primaryKeyFilter", DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, mock(IColumnFilter.class));
	}

	@Test
	public void testBatchSize() {
		doTest("batchSize", DatabaseConfig.PROPERTY_BATCH_SIZE, new Integer(123));
	}

	@Test
	public void testFetchSize() {
		doTest("fetchSize", DatabaseConfig.PROPERTY_FETCH_SIZE, new Integer(123));
	}

	@Test
	public void testMetadataHandler() {
		doTest("metadataHandler", DatabaseConfig.PROPERTY_METADATA_HANDLER, mock(IMetadataHandler.class));
	}

	@Test
	public void testCaseSensitiveTableNames() {
		doTest("caseSensitiveTableNames", DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, Boolean.TRUE);
	}

	@Test
	public void testQualifiedTableNames() {
		doTest("qualifiedTableNames", DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, Boolean.TRUE);
	}

	@Test
	public void testBatchedStatements() {
		doTest("batchedStatements", DatabaseConfig.FEATURE_BATCHED_STATEMENTS, Boolean.TRUE);
	}

	@Test
	public void testDatatypeWarning() {
		doTest("datatypeWarning", DatabaseConfig.FEATURE_DATATYPE_WARNING, Boolean.FALSE);
	}

	@Test
	public void testSkipOracleRecyclebinTables() {
		doTest("skipOracleRecyclebinTables", DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, Boolean.FALSE);
	}

	private void doTest(String propertyName, String databaseConfigProperty, Object newValue) {
		Object initialValue = this.configBeanWrapper.getPropertyValue(propertyName);
		Object expectedInitialValue = this.defaultConfig.getProperty(databaseConfigProperty);

		if ((initialValue != null) && CLASS_COMPARE_ONLY.contains(initialValue.getClass())) {
			assertEquals("Initial value is not as expected", initialValue.getClass(), expectedInitialValue.getClass());

		} else {
			assertEquals("Initial value is not as expected", initialValue, expectedInitialValue);
		}

		assertFalse("Unable to test if new value is same as intial value", newValue.equals(initialValue));
		this.configBeanWrapper.setPropertyValue(propertyName, newValue);
		DatabaseConfig appliedConfig = new DatabaseConfig();
		this.configBean.apply(appliedConfig);

		assertEquals("Did not replace " + propertyName + " value", newValue,
				appliedConfig.getProperty(databaseConfigProperty));

	}
}
