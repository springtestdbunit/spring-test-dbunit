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
package org.springframework.test.dbunit;

import org.dbunit.dataset.IDataSet;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestContext;

/**
 * Abstract data set loader, which provides a basis for concrete implementations of the {@link DataSetLoader} strategy.
 * Provides a <em>Template Method</em> based approach for {@link # loadDataSet(TestContext, String) loading} data using
 * a Spring {@link #getResourceLoader resource loader}.
 * 
 * @author Phillip Webb
 * 
 * @see #getResourceLoader
 * @see #getResourceLocations
 * @see #createDataSet(Resource)
 */
public abstract class AbstractDataSetLoader implements DataSetLoader {

	/**
	 * Loads a {@link IDataSet dataset} from {@link Resource}s obtained from the specified <tt>location</tt>. Each
	 * <tt>location</tt> can be mapped to a number of potential {@link #getResourceLocations resources}, the first
	 * resource that {@link Resource#exists() exists} will be used. {@link Resource}s are loaded using the
	 * {@link ResourceLoader} returned from {@link #getResourceLoader}.
	 * <p>
	 * If no resource can be found then <tt>null</tt> will be returned.
	 * 
	 * @see #createDataSet(Resource)
	 * @see org.springframework.test.dbunit.DataSetLoader#loadDataSet(org.springframework.test.context.TestContext,
	 * java.lang.String)
	 */
	public IDataSet loadDataSet(TestContext testContext, String location) throws Exception {
		ResourceLoader resourceLoader = getResourceLoader(testContext);
		String[] resourceLocations = getResourceLocations(testContext, location);
		for (String resourceLocation : resourceLocations) {
			Resource resource = resourceLoader.getResource(resourceLocation);
			if (resource.exists()) {
				return createDataSet(resource);
			}
		}
		return null;
	}

	/**
	 * Gets the {@link ResourceLoader} that will be used to load the dataset {@link Resource}s.
	 * @param testContext The test context
	 * @return a resource loader
	 */
	protected ResourceLoader getResourceLoader(TestContext testContext) {
		return new ClassRelativeResourceLoader(testContext.getTestClass());
	}

	/**
	 * Get the resource locations that should be considered when attempting to load a dataset from the specified
	 * location.
	 * @param testContext The test context
	 * @param location The source location
	 * @return an array of potential resource locations
	 */
	protected String[] getResourceLocations(TestContext testContext, String location) {
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
