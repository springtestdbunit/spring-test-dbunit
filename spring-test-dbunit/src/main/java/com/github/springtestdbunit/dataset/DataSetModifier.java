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

package com.github.springtestdbunit.dataset;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;

/**
 * Strategy interface that can be used to modify a {@link IDataSet}.
 *
 * @author Phillip Webb
 */
public interface DataSetModifier {

	/**
	 * No-op {@link DataSetModifier}.
	 */
	public static final DataSetModifier NONE = new DataSetModifier() {

		public IDataSet modify(IDataSet dataSet) {
			return dataSet;
		}

	};

	/**
	 * Modify the given {@link IDataSet}, for example by wrapping it with a {@link ReplacementDataSet}.
	 * @param dataSet the {@link IDataSet} to modify
	 * @return the modified {@link IDataSet}
	 */
	IDataSet modify(IDataSet dataSet);

}
