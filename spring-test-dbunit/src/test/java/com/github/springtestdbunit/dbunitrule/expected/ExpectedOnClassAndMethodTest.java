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
package com.github.springtestdbunit.dbunitrule.expected;

import javax.sql.DataSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitRule;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.github.springtestdbunit.entity.EntityAssert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/dbunit-context.xml")
@ExpectedDatabase(value = "/META-INF/db/expected_nonstrict.xml")
@Transactional
public class ExpectedOnClassAndMethodTest {

	@Rule
	public DbUnitRule dbUnit = new DbUnitRule();

	@Autowired
	DataSource dataSource;

	@Autowired
	private EntityAssert entityAssert;

	@Test
	@ExpectedDatabase(value = "/META-INF/db/expected_nonstrict.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
	public void shouldUseMethodExpectation() {
		this.entityAssert.assertValues("existing1", "existing2");
	}
}
