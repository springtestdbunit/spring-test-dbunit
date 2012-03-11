package org.github.springtestdbunit.dbunitrule.expected;

import org.github.springtestdbunit.annotation.ExpectedDatabase;
import org.github.springtestdbunit.testutils.MustNotSwallowSpringJUnit4ClassRunner;
import org.github.springtestdbunit.testutils.NotSwallowedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
