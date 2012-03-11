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
package org.github.springtestdbunit.bean;

import java.util.HashMap;
import java.util.Map;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConfig.ConfigProperty;
import org.dbunit.database.IMetadataHandler;
import org.dbunit.database.IResultSetTableFactory;
import org.dbunit.database.statement.IStatementFactory;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.filter.IColumnFilter;
import org.springframework.util.Assert;

/**
 * A bean representation of the DB unit {@link DatabaseConfig} class. This bean allows the database configuration from
 * spring using standard property arguments. The configuration from this bean can be {@link #apply applied} to an
 * existing {@link DatabaseConfig}.
 * 
 * @author Phillip Webb
 */
public class DatabaseConfigBean {

	private static final Map<String, ConfigProperty> CONFIG_PROPERTIES;
	static {
		CONFIG_PROPERTIES = new HashMap<String, ConfigProperty>();
		for (ConfigProperty configProperty : DatabaseConfig.ALL_PROPERTIES) {
			CONFIG_PROPERTIES.put(configProperty.getProperty(), configProperty);
		}
	}

	private DatabaseConfig databaseConfig = new DatabaseConfig();

	/**
	 * Gets the statement factory database config property.
	 * @return the statement factory
	 * @see DatabaseConfig#PROPERTY_STATEMENT_FACTORY
	 */
	public IStatementFactory getStatementFactory() {
		return (IStatementFactory) getProperty("statementFactory", DatabaseConfig.PROPERTY_STATEMENT_FACTORY);
	}

	/**
	 * Sets the statement factory database config property.
	 * @param statementFactory the statement factory
	 * @see DatabaseConfig#PROPERTY_STATEMENT_FACTORY
	 */
	public void setStatementFactory(IStatementFactory statementFactory) {
		setProperty("statementFactory", DatabaseConfig.PROPERTY_STATEMENT_FACTORY, statementFactory);
	}

	/**
	 * Gets the result set table factory database config property.
	 * @return the result set table factory
	 * @see DatabaseConfig#PROPERTY_RESULTSET_TABLE_FACTORY
	 */
	public IResultSetTableFactory getResultsetTableFactory() {
		return (IResultSetTableFactory) getProperty("resultSetTableFactory",
				DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY);
	}

	/**
	 * Sets the result set table factory database config property.
	 * @param resultSetTableFactory the result set table factory
	 * @see DatabaseConfig#PROPERTY_RESULTSET_TABLE_FACTORY
	 */
	public void setResultsetTableFactory(IResultSetTableFactory resultSetTableFactory) {
		setProperty("resultSetTableFactory", DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, resultSetTableFactory);
	}

	/**
	 * Gets the data type factory database config property.
	 * @return the data type factory
	 * @see DatabaseConfig#PROPERTY_DATATYPE_FACTORY
	 */
	public IDataTypeFactory getDatatypeFactory() {
		return (IDataTypeFactory) getProperty("dataTypeFactory", DatabaseConfig.PROPERTY_DATATYPE_FACTORY);
	}

	/**
	 * Sets the data type factory database config property.
	 * @param dataTypeFactory the data type factory
	 * @see DatabaseConfig#PROPERTY_DATATYPE_FACTORY
	 */
	public void setDatatypeFactory(IDataTypeFactory dataTypeFactory) {
		setProperty("dataTypeFactory", DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dataTypeFactory);
	}

	/**
	 * Gets the escape pattern database config property.
	 * @return the escape pattern
	 * @see DatabaseConfig#PROPERTY_ESCAPE_PATTERN
	 */
	public String getEscapePattern() {
		return (String) getProperty("escapePattern", DatabaseConfig.PROPERTY_ESCAPE_PATTERN);
	}

	/**
	 * Sets the escape pattern database config property.
	 * @param escapePattern the escape pattern
	 * @see DatabaseConfig#PROPERTY_ESCAPE_PATTERN
	 */
	public void setEscapePattern(String escapePattern) {
		setProperty("escapePattern", DatabaseConfig.PROPERTY_ESCAPE_PATTERN, escapePattern);
	}

	/**
	 * Gets the table type database config property.
	 * @return the table type
	 * @see DatabaseConfig#PROPERTY_TABLE_TYPE
	 */
	public String[] getTableType() {
		return (String[]) getProperty("tableTable", DatabaseConfig.PROPERTY_TABLE_TYPE);
	}

	/**
	 * Sets the table type database config property.
	 * @param tableTable the table type
	 * @see DatabaseConfig#PROPERTY_TABLE_TYPE
	 */
	public void setTableType(String[] tableTable) {
		setProperty("tableTable", DatabaseConfig.PROPERTY_TABLE_TYPE, tableTable);
	}

	/**
	 * Gets the primary key filter database config property.
	 * @return the primary key filter
	 * @see DatabaseConfig#PROPERTY_PRIMARY_KEY_FILTER
	 */
	public IColumnFilter getPrimaryKeyFilter() {
		return (IColumnFilter) getProperty("primaryKeyFilter", DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER);
	}

	/**
	 * Sets the primary key filter database config property.
	 * @param primaryKeyFilter the primary key filter
	 * @see DatabaseConfig#PROPERTY_PRIMARY_KEY_FILTER
	 */
	public void setPrimaryKeyFilter(IColumnFilter primaryKeyFilter) {
		setProperty("primaryKeyFilter", DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, primaryKeyFilter);
	}

	/**
	 * Gets the batch size database config property.
	 * @return the batch size
	 * @see DatabaseConfig#PROPERTY_BATCH_SIZE
	 */
	public Integer getBatchSize() {
		return (Integer) getProperty("batchSize", DatabaseConfig.PROPERTY_BATCH_SIZE);
	}

	/**
	 * Sets the batch size database config property.
	 * @param batchSize the batch size
	 * @see DatabaseConfig#PROPERTY_BATCH_SIZE
	 */
	public void setBatchSize(Integer batchSize) {
		setProperty("batchSize", DatabaseConfig.PROPERTY_BATCH_SIZE, batchSize);
	}

	/**
	 * Gets the fetch size database config property.
	 * @return the fetch size
	 * @see DatabaseConfig#PROPERTY_FETCH_SIZE
	 */
	public Integer getFetchSize() {
		return (Integer) getProperty("fetchSize", DatabaseConfig.PROPERTY_FETCH_SIZE);
	}

	/**
	 * Sets the fetch size database config property.
	 * @param fetchSize the fetch size
	 * @see DatabaseConfig#PROPERTY_FETCH_SIZE
	 */
	public void setFetchSize(Integer fetchSize) {
		setProperty("fetchSize", DatabaseConfig.PROPERTY_FETCH_SIZE, fetchSize);
	}

	/**
	 * Gets the meta-data handler database config property.
	 * @return the meta-data handler
	 * @see DatabaseConfig#PROPERTY_METADATA_HANDLER
	 */
	public IMetadataHandler getMetadataHandler() {
		return (IMetadataHandler) getProperty("metadataHandler", DatabaseConfig.PROPERTY_METADATA_HANDLER);
	}

	/**
	 * Sets the meta-data handler database config property.
	 * @param metadataHandler meta-data handler
	 * @see DatabaseConfig#PROPERTY_METADATA_HANDLER
	 */
	public void setMetadataHandler(IMetadataHandler metadataHandler) {
		setProperty("metadataHandler", DatabaseConfig.PROPERTY_METADATA_HANDLER, metadataHandler);
	}

	/**
	 * Gets the case sensitive table names database config feature.
	 * @return case sensitive table names
	 * @see DatabaseConfig#FEATURE_CASE_SENSITIVE_TABLE_NAMES
	 */
	public Boolean getCaseSensitiveTableNames() {
		return (Boolean) getProperty("caseSensitiveTableNames", DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES);
	}

	/**
	 * Sets the case sensitive table names database config feature.
	 * @param caseSensitiveTableNames case sensitive table names
	 * @see DatabaseConfig#FEATURE_CASE_SENSITIVE_TABLE_NAMES
	 */
	public void setCaseSensitiveTableNames(Boolean caseSensitiveTableNames) {
		setProperty("caseSensitiveTableNames", DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES,
				caseSensitiveTableNames);
	}

	/**
	 * Gets the qualified table names database config feature.
	 * @return the qualified table names
	 * @see DatabaseConfig#FEATURE_QUALIFIED_TABLE_NAMES
	 */
	public Boolean getQualifiedTableNames() {
		return (Boolean) getProperty("qualifiedTableNames", DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES);
	}

	/**
	 * Sets the qualified table names database config feature.
	 * @param qualifiedTableNames the qualified table names
	 * @see DatabaseConfig#FEATURE_QUALIFIED_TABLE_NAMES
	 */
	public void setQualifiedTableNames(Boolean qualifiedTableNames) {
		setProperty("qualifiedTableNames", DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, qualifiedTableNames);
	}

	/**
	 * Gets the batched statements database config feature.
	 * @return the batched statements
	 * @see DatabaseConfig#FEATURE_BATCHED_STATEMENTS
	 */
	public Boolean getBatchedStatements() {
		return (Boolean) getProperty("batchedStatements", DatabaseConfig.FEATURE_BATCHED_STATEMENTS);
	}

	/**
	 * Sets the batched statements database config feature.
	 * @param batchedStatements the batched statements
	 * @see DatabaseConfig#FEATURE_BATCHED_STATEMENTS
	 */
	public void setBatchedStatements(Boolean batchedStatements) {
		setProperty("batchedStatements", DatabaseConfig.FEATURE_BATCHED_STATEMENTS, batchedStatements);
	}

	/**
	 * Gets the datatype warning database config feature.
	 * @return the datatype warning
	 * @see DatabaseConfig#FEATURE_DATATYPE_WARNING
	 */
	public Boolean getDatatypeWarning() {
		return (Boolean) getProperty("datatypeWarning", DatabaseConfig.FEATURE_DATATYPE_WARNING);
	}

	/**
	 * Sets the datatype warning database config feature.
	 * @param datatypeWarning the datatype warning
	 * @see DatabaseConfig#FEATURE_DATATYPE_WARNING
	 */
	public void setDatatypeWarning(Boolean datatypeWarning) {
		setProperty("datatypeWarning", DatabaseConfig.FEATURE_DATATYPE_WARNING, datatypeWarning);
	}

	/**
	 * Gets the skip oracle recyclebin tables database config feature.
	 * @return the skip oracle recyclebin tables
	 * @see DatabaseConfig#FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES
	 */
	public Boolean getSkipOracleRecyclebinTables() {
		return (Boolean) getProperty("skipOracleRecyclebinTables", DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES);
	}

	/**
	 * Sets the skip oracle recyclebin tables database config feature.
	 * @param skipOracleRecyclebinTables skip oracle recyclebin tables
	 * @see DatabaseConfig#FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES
	 */
	public void setSkipOracleRecyclebinTables(Boolean skipOracleRecyclebinTables) {
		setProperty("skipOracleRecyclebinTables", DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES,
				skipOracleRecyclebinTables);
	}

	/**
	 * Get a property from the underlying data config.
	 * @param propertyName The name of the attribute
	 * @param dataConfigPropertyName The data config property name
	 * @return the value of the property
	 */
	private Object getProperty(String propertyName, String dataConfigPropertyName) {
		return this.databaseConfig.getProperty(dataConfigPropertyName);
	}

	/**
	 * Set a property to the underlying data config.
	 * @param propertyName the name of the property
	 * @param dataConfigPropertyName the data config property name
	 * @param value the value to set
	 */
	private void setProperty(String propertyName, String dataConfigPropertyName, Object value) {
		ConfigProperty configProperty = CONFIG_PROPERTIES.get(dataConfigPropertyName);
		Assert.state(configProperty != null, "Unsupported config property " + dataConfigPropertyName + " for "
				+ propertyName);
		if (!configProperty.isNullable()) {
			Assert.notNull(value, propertyName + " cannot be null");
		}
		if (value != null) {
			Assert.isInstanceOf(configProperty.getPropertyType(), value, "Unable to set " + propertyName + " ");
		}
		this.databaseConfig.setProperty(dataConfigPropertyName, value);
	}

	/**
	 * Apply the configuration represented by this bean to the specified databaseConfig.
	 * @param databaseConfig the database config to be updated.
	 */
	public void apply(DatabaseConfig databaseConfig) {
		for (ConfigProperty configProperty : DatabaseConfig.ALL_PROPERTIES) {
			String name = configProperty.getProperty();
			Object value = this.databaseConfig.getProperty(name);
			if ((configProperty.isNullable()) || ((!configProperty.isNullable()) && (value != null))) {
				databaseConfig.setProperty(name, value);
			}
		}
	}
}
