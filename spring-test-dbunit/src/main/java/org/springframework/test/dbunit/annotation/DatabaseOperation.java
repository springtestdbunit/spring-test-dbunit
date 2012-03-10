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
package org.springframework.test.dbunit.annotation;

/**
 * Database test operations that can be performed to configure database tables.
 * 
 * @see DatabaseSetup
 * @see DatabaseTearDown
 * 
 * @author Phillip Webb
 */
public enum DatabaseOperation {

	/**
	 * Updates the contents of existing database tables from the dataset.
	 */
	UPDATE,

	/**
	 * Inserts new database tables and contents from the dataset.
	 */
	INSERT,

	/**
	 * Refresh the contents of existing database tables. Rows from the dataset will insert or replace existing data. Any
	 * database rows that are not in the dataset remain unaffected.
	 */
	REFRESH,

	/**
	 * Deletes database table rows that matches rows from the dataset.
	 */
	DELETE,

	/**
	 * Deletes all rows from a database table when the table is specified in the dataset. Tables in the database but not
	 * in the dataset remain unaffected.
	 * @see #TRUNCATE_TABLE
	 */
	DELETE_ALL,

	/**
	 * Deletes all rows from a database table when the table is specified in the dataset. Tables in the database but not
	 * in the dataset are unaffected. Identical to {@link #DELETE_ALL} expect this operation cannot be rolled back and
	 * is supported by less database vendors.
	 * @see #DELETE_ALL
	 */
	TRUNCATE_TABLE,

	/**
	 * Deletes all rows from a database table when the tables is specified in the dataset and subsequently insert new
	 * contents. Equivalent to calling {@link #DELETE_ALL} followed by {@link #INSERT}.
	 */
	CLEAN_INSERT;

}
