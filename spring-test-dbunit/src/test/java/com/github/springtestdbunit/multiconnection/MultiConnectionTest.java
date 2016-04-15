/*
 * Copyright 2002-2016 the original author or authors
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

package com.github.springtestdbunit.multiconnection;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.entity.EntityAssert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/dbunit-context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		TransactionDbUnitTestExecutionListener.class, })
@DbUnitConfiguration(databaseConnection = { "dataSource", "dataSource2" })
@DatabaseSetup("/META-INF/db/insert.xml")
@DatabaseSetup(connection = "dataSource2", value = "/META-INF/db/multi-insert.xml")
@Transactional
public class MultiConnectionTest {

	@Autowired
	private EntityAssert entityAssert;

	@Autowired
	@Qualifier("dataSource2")
	private DataSource dataSource;

	@Test
	@DatabaseSetup(value = "/META-INF/db/insert2.xml", type = DatabaseOperation.INSERT)
	@DatabaseSetup(connection = "dataSource2", value = "/META-INF/db/multi-insert2.xml", type = DatabaseOperation.INSERT)
	public void testInsert() throws Exception {
		this.entityAssert.assertValues("fromDbUnit", "fromDbUnit2");
		assertSecondDataSourceValues("fromDbUnitSecondConnection", "fromDbUnitSecondConnection2");
	}

	@Test
	@DatabaseSetup(value = "/META-INF/db/refresh.xml", type = DatabaseOperation.REFRESH)
	@DatabaseSetup(connection = "dataSource2", value = "/META-INF/db/multi-refresh.xml", type = DatabaseOperation.REFRESH)
	public void testRefresh() throws Exception {
		this.entityAssert.assertValues("addedFromDbUnit", "replacedFromDbUnit");
		assertSecondDataSourceValues("addedFromDbUnitSecondConnection", "replacedFromDbUnitSecondConnection");
	}

	@Test
	@ExpectedDatabase(connection = "dataSource2", value = "/META-INF/db/multi-expected.xml")
	public void testExpected() throws Exception {
		JdbcTemplate jdbc = new JdbcTemplate(this.dataSource);
		jdbc.execute("insert into second(id, value) values (200, 'abc')");
	}

	private void assertSecondDataSourceValues(String... expected) {
		JdbcTemplate jdbc = new JdbcTemplate(this.dataSource);
		List<String> actual = jdbc.queryForList("select value from second", String.class);
		assertEquals(new HashSet<String>(Arrays.asList(expected)), new HashSet<String>(actual));
	}

}
