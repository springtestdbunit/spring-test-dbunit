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

package com.github.springtestdbunit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.dataset.IDataSet;

import com.github.springtestdbunit.dataset.DataSetModifier;

/**
 * A collection of {@link DataSetModifier} items loaded during testing.
 *
 * @author Phillip Webb
 */
class DataSetModifiers implements DataSetModifier {

	private final List<DataSetModifier> modifiers = new ArrayList<DataSetModifier>();

	public IDataSet modify(IDataSet dataSet) {
		for (DataSetModifier modifier : this.modifiers) {
			dataSet = modifier.modify(dataSet);
		}
		return dataSet;
	}

	public void add(Object testInstance, Class<? extends DataSetModifier> modifierClass) {
		try {
			Class<?> enclosingClass = modifierClass.getEnclosingClass();
			if ((enclosingClass == null) || Modifier.isStatic(modifierClass.getModifiers())) {
				add(modifierClass.getDeclaredConstructor());
			} else {
				add(modifierClass.getDeclaredConstructor(enclosingClass), testInstance);
			}
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private void add(Constructor<? extends DataSetModifier> constructor, Object... args) throws Exception {
		constructor.setAccessible(true);
		this.modifiers.add(constructor.newInstance(args));
	}

}
