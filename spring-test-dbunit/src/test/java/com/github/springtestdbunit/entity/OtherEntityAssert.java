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
 * Bean that can be used to assert the {@link com.github.springtestdbunit.entity.OtherSampleEntity#getValue() values}
 * from {@link com.github.springtestdbunit.entity.OtherSampleEntity entities} contained in the database.
 *
 * @author Oleksii Lomako
 */
public class OtherEntityAssert implements InitializingBean {

	@PersistenceContext
	private EntityManager entityManager;

	private CriteriaQuery<OtherSampleEntity> criteriaQuery;

	public void afterPropertiesSet() throws Exception {
		CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
		this.criteriaQuery = cb.createQuery(OtherSampleEntity.class);
		Root<OtherSampleEntity> from = this.criteriaQuery.from(OtherSampleEntity.class);
		this.criteriaQuery.orderBy(cb.asc(from.get("value").as(String.class)));
	}

	public void assertValues(String... values) {
		SortedSet<String> expected = new TreeSet<String>(Arrays.asList(values));
		SortedSet<String> actual = new TreeSet<String>();
		TypedQuery<OtherSampleEntity> query = this.entityManager.createQuery(this.criteriaQuery);
		List<OtherSampleEntity> results = query.getResultList();
		for (OtherSampleEntity sampleEntity : results) {
			actual.add(sampleEntity.getValue());
			this.entityManager.detach(sampleEntity);
		}
		assertEquals(expected, actual);
	}

}
