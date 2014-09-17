/*
 * Copyright 2002-2013 the original author or authors
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

package com.github.springtestdbunit.operation;

import static org.junit.Assert.assertSame;

import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.junit.Test;

import com.github.springtestdbunit.annotation.DatabaseOperation;

/**
 * Tests for {@link MicrosoftSqlDatabaseOperationLookup}.
 *
 * @author Phillip Webb
 */
public class MicrosoftSqlDatabaseOperationLookupTest {

	@Test
	public void shouldLookup() throws Exception {
		DefaultDatabaseOperationLookup lookup = new MicrosoftSqlDatabaseOperationLookup();
		assertSame(org.dbunit.operation.DatabaseOperation.UPDATE, lookup.get(DatabaseOperation.UPDATE));
		assertSame(InsertIdentityOperation.INSERT, lookup.get(DatabaseOperation.INSERT));
		assertSame(InsertIdentityOperation.REFRESH, lookup.get(DatabaseOperation.REFRESH));
		assertSame(org.dbunit.operation.DatabaseOperation.DELETE, lookup.get(DatabaseOperation.DELETE));
		assertSame(org.dbunit.operation.DatabaseOperation.DELETE_ALL, lookup.get(DatabaseOperation.DELETE_ALL));
		assertSame(org.dbunit.operation.DatabaseOperation.TRUNCATE_TABLE, lookup.get(DatabaseOperation.TRUNCATE_TABLE));
		assertSame(InsertIdentityOperation.CLEAN_INSERT, lookup.get(DatabaseOperation.CLEAN_INSERT));
	}
}
