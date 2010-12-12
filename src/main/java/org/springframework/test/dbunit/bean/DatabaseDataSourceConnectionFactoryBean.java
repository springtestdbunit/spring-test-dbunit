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

import javax.sql.DataSource;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

/**
 * A {@link FactoryBean} that can be used to create a {@link #setTransactionAware transaction} aware
 * {@link DatabaseDataSourceConnection} using the specified {@link #setDataSource dataSource}. Additional configuration
 * is also supported using {@link #setDatabaseConfig(DatabaseConfigBean)}.
 * 
 * @author Phillip Webb
 */
public class DatabaseDataSourceConnectionFactoryBean implements FactoryBean<DatabaseDataSourceConnection> {

	private DataSource dataSource;
	private boolean transactionAware = true;
	private String username;
	private String password;
	private String schema;
	private DatabaseConfigBean databaseConfig;

	public DatabaseDataSourceConnection getObject() throws Exception {
		Assert.notNull(dataSource, "The dataSource is required");
		DatabaseDataSourceConnection dataSourceConntection = new DatabaseDataSourceConnection(
				makeTransactionAware(dataSource), schema, username, password);
		if (databaseConfig != null) {
			databaseConfig.apply(dataSourceConntection.getConfig());
		}
		return dataSourceConntection;
	}

	private DataSource makeTransactionAware(DataSource dataSource) {
		if (dataSource instanceof TransactionAwareDataSourceProxy || !transactionAware) {
			return dataSource;
		}
		return new TransactionAwareDataSourceProxy(dataSource);
	}

	public Class<?> getObjectType() {
		return DatabaseDataSourceConnection.class;
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Set the data source that will be used for the {@link DatabaseDataSourceConnection}. This value must be set before
	 * the connection can be created.
	 * @param dataSource the data source
	 */
	public void setDataSource(DataSource dataSource) {
		Assert.notNull(dataSource, "The dataSource is required.");
		this.dataSource = dataSource;
	}

	/**
	 * Set the user name to use when accessing the data source.
	 * @param username the user name or <tt>null</tt>
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Set the password to use when accessing the data source.
	 * @param password the password or <tt>null</tt>
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Set the schema to use when accessing the data source.
	 * @param schema the schema or <tt>null</tt>
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 * Set an optional {@link DatabaseConfigBean configuration} that will be applied to the newly created
	 * {@link DatabaseDataSourceConnection}
	 * 
	 * @param databaseConfig the database configuration or <tt>null</tt> if no additional configuration is required.
	 */
	public void setDatabaseConfig(DatabaseConfigBean databaseConfig) {
		this.databaseConfig = databaseConfig;
	}

	/**
	 * Determines if the {@link IDatabaseConnection} created by this bean should be aware of Spring
	 * {@link PlatformTransactionManager}s. Defaults to <tt>true</tt>
	 * @param transactionAware If the connection should be transaction aware
	 */
	public void setTransactionAware(boolean transactionAware) {
		this.transactionAware = transactionAware;
	}
}
