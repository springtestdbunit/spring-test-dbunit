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

package com.github.springtestdbunit.operation;

import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.github.springtestdbunit.annotation.DatabaseOperation;

/**
 * Tests for {@link DefaultDatabaseOperationLookup}.
 *
 * @author Phillip Webb
 */
public class DefaultDatabaseOperationLookupTest {

	@Test
	public void shouldLookup() throws Exception {
		DefaultDatabaseOperationLookup lookup = new DefaultDatabaseOperationLookup();
		assertSame(org.dbunit.operation.DatabaseOperation.UPDATE, lookup.get(DatabaseOperation.UPDATE));
		assertSame(org.dbunit.operation.DatabaseOperation.INSERT, lookup.get(DatabaseOperation.INSERT));
		assertSame(org.dbunit.operation.DatabaseOperation.REFRESH, lookup.get(DatabaseOperation.REFRESH));
		assertSame(org.dbunit.operation.DatabaseOperation.DELETE, lookup.get(DatabaseOperation.DELETE));
		assertSame(org.dbunit.operation.DatabaseOperation.DELETE_ALL, lookup.get(DatabaseOperation.DELETE_ALL));
		assertSame(org.dbunit.operation.DatabaseOperation.TRUNCATE_TABLE, lookup.get(DatabaseOperation.TRUNCATE_TABLE));
		assertSame(org.dbunit.operation.DatabaseOperation.CLEAN_INSERT, lookup.get(DatabaseOperation.CLEAN_INSERT));
	}

}
