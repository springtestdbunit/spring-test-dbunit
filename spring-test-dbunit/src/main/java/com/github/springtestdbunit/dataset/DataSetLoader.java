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

package com.github.springtestdbunit.dataset;

import org.dbunit.dataset.IDataSet;

/**
 * Strategy interface for {@link #loadDataSet loading} a {@link IDataSet dataset}.
 * <p>
 * Concrete implementations must provide a <code>public</code> no-args constructor.
 *
 * @author Phillip Webb
 *
 * @see FlatXmlDataSetLoader
 */
public interface DataSetLoader {

	/**
	 * Load and return {@link IDataSet dataset} from the specified. If the dataset cannot be found <tt>null</tt> may be
	 * returned.
	 * @param testClass The class under test
	 * @param location The location to load
	 * @return a {@link IDataSet dataset} or <tt>null</tt>
	 * @throws Exception If the {@link IDataSet dataset} cannot be loaded
	 */
	public IDataSet loadDataSet(Class<?> testClass, String location) throws Exception;

}
