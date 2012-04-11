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
package com.github.springtestdbunit.dataset;

import java.io.InputStream;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.springframework.core.io.Resource;

/**
 * A {@link DataSetLoader data set loader} that can be used to load {@link FlatXmlDataSet xml datasets}
 * 
 * @author Phillip Webb
 */
public class FlatXmlDataSetLoader extends AbstractDataSetLoader {

	@Override
	protected IDataSet createDataSet(Resource resource) throws Exception {
		FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
		InputStream inputStream = resource.getInputStream();
		try {
			return builder.build(inputStream);
		} finally {
			inputStream.close();
		}
	}
}
