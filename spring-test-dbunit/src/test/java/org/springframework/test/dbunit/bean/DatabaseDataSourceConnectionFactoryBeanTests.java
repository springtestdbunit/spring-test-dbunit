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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

/**
 * Tests for {@link DatabaseDataSourceConnectionFactoryBean}.
 * 
 * @author Phillip Webb
 */
public class DatabaseDataSourceConnectionFactoryBeanTests {

	private DatabaseDataSourceConnectionFactoryBean factoryBean;

	@Before
	public void setup() {
		this.factoryBean = new DatabaseDataSourceConnectionFactoryBean();
	}

	@Test
	public void shouldNotAllowObjectWithoutDataSet() throws Exception {
		try {
			factoryBean.getObject();
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The dataSource is required", e.getMessage());
		}
	}

	@Test
	public void shouldCreateDatabaseDataSourceConnection() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		given(dataSource.getConnection()).willReturn(connection);
		factoryBean.setDataSource(dataSource);
		DatabaseDataSourceConnection bean = factoryBean.getObject();
		assertNotNull(bean);
		bean.getConnection().createStatement();
		verify(dataSource).getConnection();
	}

	@Test
	public void shouldAcceptUsernameAndPassword() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		factoryBean.setDataSource(dataSource);
		factoryBean.setUsername("username");
		factoryBean.setPassword("password");
		DatabaseDataSourceConnection bean = factoryBean.getObject();
		assertNotNull(bean);
		bean.getConnection();
		verify(dataSource).getConnection("username", "password");
	}

	@Test
	public void shouldSupportSchema() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		factoryBean.setDataSource(dataSource);
		factoryBean.setSchema("schema");
		DatabaseDataSourceConnection bean = factoryBean.getObject();
		assertEquals("schema", bean.getSchema());
	}

	@Test
	public void shouldSupportDatabaseConfigBean() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		factoryBean.setDataSource(dataSource);
		DatabaseConfigBean databaseConfig = mock(DatabaseConfigBean.class);
		factoryBean.setDatabaseConfig(databaseConfig);
		DatabaseDataSourceConnection bean = factoryBean.getObject();
		assertNotNull(bean);
		verify(databaseConfig).apply(bean.getConfig());
	}

	@Test
	public void shouldBeSingleton() throws Exception {
		assertTrue(factoryBean.isSingleton());
	}

	@Test
	public void shouldBeCorrectClass() throws Exception {
		assertEquals(DatabaseDataSourceConnection.class, factoryBean.getObjectType());
	}

	@Test
	public void shouldCreateCreateTransactionAwareConnection() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		factoryBean.setDataSource(dataSource);
		DatabaseDataSourceConnection dataSourceConnection = factoryBean.getObject();
		Connection connection = dataSourceConnection.getConnection();
		assertTrue(connection.toString() + " is not transaction aware",
				connection.toString().startsWith("Transaction-aware proxy"));
	}

	@Test
	public void shouldNotWrapCreateTransactionAwareConnection() throws Exception {
		DataSource dataSource = new TransactionAwareDataSourceProxy(mock(DataSource.class));
		factoryBean.setDataSource(dataSource);
		DatabaseDataSourceConnection dataSourceConnection = factoryBean.getObject();
		Connection connection = dataSourceConnection.getConnection();
		assertTrue(
				connection.toString() + " is not transaction aware",
				connection.toString().startsWith(
						"Transaction-aware proxy for target Connection  from DataSource [Mock for DataSource"));
	}

	@Test
	public void shouldRespectTransactionAwareAttribute() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		given(dataSource.getConnection()).willReturn(connection);
		factoryBean.setDataSource(dataSource);
		factoryBean.setTransactionAware(false);
		DatabaseDataSourceConnection dataSourceConnection = factoryBean.getObject();
		Connection actual = dataSourceConnection.getConnection();
		assertSame(connection, actual);
	}

	@Test
	public void shouldSupportNewConnection() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		given(dataSource.getConnection()).willReturn(connection);
		IDatabaseConnection databaseConnection = DatabaseDataSourceConnectionFactoryBean.newConnection(dataSource);
		assertNotNull(databaseConnection);
		databaseConnection.getConnection().createStatement();
		verify(dataSource).getConnection();
	}
}
