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

package com.github.springtestdbunit.dataset;

import java.util.ArrayList;
import java.util.List;

import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Abstract data set loader, which provides a basis for concrete implementations of the {@link DataSetLoader} strategy.
 * Provides a <em>Template Method</em> based approach for {@link #loadDataSet(Class, String) loading} data using a
 * Spring {@link #getResourceLoader resource loader}.
 * 
 * @author Phillip Webb
 * @author Vseslav Suvorov
 * 
 * @see #getResourceLoader
 * @see #getResourceLocations
 * @see #createDataSet(Resource)
 */
public abstract class AbstractDataSetLoader implements DataSetLoader, CompositeDataSetLoader {

	/**
	 * Loads a {@link IDataSet dataset} from {@link Resource}s obtained from the specified <tt>location</tt>. Each
	 * <tt>location</tt> can be mapped to a number of potential {@link #getResourceLocations resources}, the first
	 * resource that {@link Resource#exists() exists} will be used. {@link Resource}s are loaded using the
	 * {@link ResourceLoader} returned from {@link #getResourceLoader}.
	 * <p>
	 * If no resource can be found then <tt>null</tt> will be returned.
	 * 
	 * @see #createDataSet(Resource)
	 * @see com.github.springtestdbunit.dataset.DataSetLoader#loadDataSet(Class, String) java.lang.String)
	 */
	public IDataSet loadDataSet(Class<?> testClass, String location) throws Exception {
		ResourceLoader resourceLoader = getResourceLoader(testClass);
		String[] resourceLocations = getResourceLocations(testClass, location);
		for (String resourceLocation : resourceLocations) {
			Resource resource = resourceLoader.getResource(resourceLocation);
			if (resource.exists()) {
				return createDataSet(resource);
			}
		}
		return null;
	}

	/**
	 * Load and return a composite {@link IDataSet dataset} from the multiple {@code locations}. Each location is loaded
	 * using {@link AbstractDataSetLoader#loadDataSet(Class, String)} and then all the datasets are combined into the
	 * single dataset.
	 * <p>
	 * If none of resources can be found then {@code null} will be returned.
	 * 
	 * @param testClass The class under test
	 * @param locations The locations to load
	 * @param combine if <tt>true</tt>, tables having the same name are merged into one table
	 * @param caseSensitiveTableNames Whether or not table names are handled in a case sensitive way over all datasets
	 * @return a {@link IDataSet dataset} or {@code null}
	 * @throws Exception If the {@link IDataSet dataset} cannot be loaded
	 */
	public IDataSet loadDataSet(Class<?> testClass, String[] locations, boolean combine, boolean caseSensitiveTableNames)
			throws Exception {
		List<IDataSet> dataSets = new ArrayList<IDataSet>(locations.length);
		for (String location : locations) {
			IDataSet dataSet = loadDataSet(testClass, location);
			if (dataSet != null) {
				dataSets.add(dataSet);
			}
		}
		return dataSets.isEmpty() ? null : new CompositeDataSet(dataSets.toArray(new IDataSet[dataSets.size()]),
				combine, caseSensitiveTableNames);
	}

	/**
	 * Gets the {@link ResourceLoader} that will be used to load the dataset {@link Resource}s.
	 * @param testClass The class under test
	 * @return a resource loader
	 */
	protected ResourceLoader getResourceLoader(Class<?> testClass) {
		return new ClassRelativeResourceLoader(testClass);
	}

	/**
	 * Get the resource locations that should be considered when attempting to load a dataset from the specified
	 * location.
	 * @param testClass The class under test
	 * @param location The source location
	 * @return an array of potential resource locations
	 */
	protected String[] getResourceLocations(Class<?> testClass, String location) {
		return new String[] { location };
	}

	/**
	 * Factory method used to create the {@link IDataSet dataset}
	 * @param resource an existing resource that contains the dataset data
	 * @return a dataset
	 * @throws Exception if the dataset could not be loaded
	 */
	protected abstract IDataSet createDataSet(Resource resource) throws Exception;
}
