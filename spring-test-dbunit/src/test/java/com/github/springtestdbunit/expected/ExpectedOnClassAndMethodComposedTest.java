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

package com.github.springtestdbunit.expected;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.github.springtestdbunit.entity.EntityAssert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/dbunit-context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@ExpectedDatabase(value = {"/META-INF/db/expectedfail_composed1.xml",
        "/META-INF/db/expectedfail_composed2.xml", "/META-INF/db/expectedfail_composed3.xml"})
@Transactional
public class ExpectedOnClassAndMethodComposedTest {

    @Autowired
    private EntityAssert entityAssert;

    @Test
    @ExpectedDatabase(
            value = {"/META-INF/db/expected_nonstrict_composed1.xml",
                    "/META-INF/db/expected_nonstrict_composed2.xml"},
            assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void shouldUseMethodExpectation() {
        this.entityAssert.assertValues("existing1", "existing2");
    }

}
