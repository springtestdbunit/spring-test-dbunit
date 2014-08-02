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

package com.github.springtestdbunit.setup;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.entity.EntityAssert;
import com.github.springtestdbunit.entity.OtherSampleEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/dbunit-context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class })
@Transactional
public class CleanInsertSetupOnMethodTest {

	@Autowired
	private EntityAssert entityAssert;
    @PersistenceContext
    EntityManager entityManager;

	@Test
    @DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT, value = "/META-INF/db/insert.xml")
    public void test() throws Exception {
		this.entityAssert.assertValues("fromDbUnit");
	}

    @Test
    @DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT, value = {"/META-INF/db/insert.xml", "/META-INF/db/insert_Other.xml"})
    public void testSeveralSetupFiles() throws Exception {
        //OtherSampleEntity is populated using OtherEntityInitializer class imitating dirty state of the table
        List<OtherSampleEntity> list = entityManager.createQuery("from OtherSampleEntity", OtherSampleEntity.class).getResultList();
        assertEquals("OtherSampleEntity table must be cleaned up using @DatabaseSetup second file", 1, list.size());
    }
}
