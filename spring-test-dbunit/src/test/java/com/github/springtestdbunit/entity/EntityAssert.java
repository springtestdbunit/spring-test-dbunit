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

package com.github.springtestdbunit.entity;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.InitializingBean;

/**
 * Bean that can be used to assert the {@link SampleEntity#getValue() values} from {@link SampleEntity entities}
 * contained in the database.
 *
 * @author Phillip Webb
 */
public class EntityAssert implements InitializingBean {

	@PersistenceContext
	private EntityManager entityManager;

	private CriteriaQuery<SampleEntity> criteriaQuery;

	public void afterPropertiesSet() throws Exception {
		CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
		this.criteriaQuery = cb.createQuery(SampleEntity.class);
		Root<SampleEntity> from = this.criteriaQuery.from(SampleEntity.class);
		this.criteriaQuery.orderBy(cb.asc(from.get("value").as(String.class)));
	}

	public void assertValues(String... values) {
		SortedSet<String> expected = new TreeSet<String>(Arrays.asList(values));
		SortedSet<String> actual = new TreeSet<String>();
		TypedQuery<SampleEntity> query = this.entityManager.createQuery(this.criteriaQuery);
		List<SampleEntity> results = query.getResultList();
		for (SampleEntity sampleEntity : results) {
			actual.add(sampleEntity.getValue());
			this.entityManager.detach(sampleEntity);
		}
		assertEquals(expected, actual);
	}

}
