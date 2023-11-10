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

import java.util.HashMap;
import java.util.Map;

import com.github.springtestdbunit.annotation.DatabaseOperation;

/**
 * Default implementation of {@link DatabaseOperationLookup}.
 *
 * @author Phillip Webb
 */
public class DefaultDatabaseOperationLookup implements DatabaseOperationLookup {

	private final static Map<DatabaseOperation, org.dbunit.operation.DatabaseOperation> OPERATION_LOOKUP;

	static {
		OPERATION_LOOKUP = new HashMap<>();
		OPERATION_LOOKUP.put(DatabaseOperation.UPDATE, org.dbunit.operation.DatabaseOperation.UPDATE);
		OPERATION_LOOKUP.put(DatabaseOperation.INSERT, org.dbunit.operation.DatabaseOperation.INSERT);
		OPERATION_LOOKUP.put(DatabaseOperation.REFRESH, org.dbunit.operation.DatabaseOperation.REFRESH);
		OPERATION_LOOKUP.put(DatabaseOperation.DELETE, org.dbunit.operation.DatabaseOperation.DELETE);
		OPERATION_LOOKUP.put(DatabaseOperation.DELETE_ALL, org.dbunit.operation.DatabaseOperation.DELETE_ALL);
		OPERATION_LOOKUP.put(DatabaseOperation.TRUNCATE_TABLE, org.dbunit.operation.DatabaseOperation.TRUNCATE_TABLE);
		OPERATION_LOOKUP.put(DatabaseOperation.CLEAN_INSERT, org.dbunit.operation.DatabaseOperation.CLEAN_INSERT);
	}

	public org.dbunit.operation.DatabaseOperation get(DatabaseOperation operation) {
		return OPERATION_LOOKUP.get(operation);
	}

}
