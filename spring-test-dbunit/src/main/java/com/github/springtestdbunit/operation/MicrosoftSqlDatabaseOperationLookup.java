/*
 * Copyright 2002-2015 the original author or authors
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

import java.util.HashMap;
import java.util.Map;

import org.dbunit.ext.mssql.InsertIdentityOperation;

import com.github.springtestdbunit.annotation.DatabaseOperation;

/**
 * Microsoft SQL Server implementation of {@link DatabaseOperationLookup}.
 *
 * @author Phillip Webb
 */
public class MicrosoftSqlDatabaseOperationLookup extends DefaultDatabaseOperationLookup {

	private static Map<DatabaseOperation, org.dbunit.operation.DatabaseOperation> MSSQL_LOOKUP;
	static {
		MSSQL_LOOKUP = new HashMap<DatabaseOperation, org.dbunit.operation.DatabaseOperation>();
		MSSQL_LOOKUP.put(DatabaseOperation.INSERT, InsertIdentityOperation.INSERT);
		MSSQL_LOOKUP.put(DatabaseOperation.REFRESH, InsertIdentityOperation.REFRESH);
		MSSQL_LOOKUP.put(DatabaseOperation.CLEAN_INSERT, InsertIdentityOperation.CLEAN_INSERT);
	}

	@Override
	public org.dbunit.operation.DatabaseOperation get(DatabaseOperation operation) {
		if (MSSQL_LOOKUP.containsKey(operation)) {
			return MSSQL_LOOKUP.get(operation);
		}
		return super.get(operation);
	}

}
