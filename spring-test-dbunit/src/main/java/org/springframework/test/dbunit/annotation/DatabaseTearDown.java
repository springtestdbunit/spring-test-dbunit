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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.dbunit.DbUnitTestExecutionListener;

/**
 * Test annotation which indicates how to put a database into a know state after tests have run. This annotation can be
 * placed on a class or on methods. When placed on a class the setup is applied after each test methods is executed.
 * 
 * @see DatabaseSetup
 * @see ExpectedDatabase
 * @see DbUnitConfiguration
 * @see DbUnitTestExecutionListener
 * 
 * @author Phillip Webb
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface DatabaseTearDown {

	/**
	 * Determines the type of {@link DatabaseOperation operation} that will be used to reset the database.
	 * @return The type of operation used to reset the database
	 */
	DatabaseOperation type() default DatabaseOperation.CLEAN_INSERT;

	/**
	 * Provides the locations of the datasets that will be used to reset the database.
	 * @return The dataset locations
	 * @see DbUnitConfiguration#dataSetLoader()
	 */
	String[] value();

}
