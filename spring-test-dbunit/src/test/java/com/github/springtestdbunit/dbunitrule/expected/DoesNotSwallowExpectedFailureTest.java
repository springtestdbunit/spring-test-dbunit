package com.github.springtestdbunit.dbunitrule.expected;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.testutils.MustNotSwallowSpringJUnit4ClassRunner;
import com.github.springtestdbunit.testutils.NotSwallowedException;

@RunWith(MustNotSwallowSpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/dbunit-context.xml")
@Transactional
public class DoesNotSwallowExpectedFailureTest {

	@Test
	@ExpectedDatabase("/META-INF/db/expectedfail.xml")
	public void test() throws Exception {
		throw new NotSwallowedException();
	}

}
