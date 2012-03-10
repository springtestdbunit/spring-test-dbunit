package org.springframework.test.dbunit.dbunittestexecutionlistener.expected;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.dbunit.annotation.ExpectedDatabase;
import org.springframework.test.dbunit.testutils.MustNoSwallowTestExecutionListener;
import org.springframework.test.dbunit.testutils.MustNotSwallowSpringJUnit4ClassRunner;
import org.springframework.test.dbunit.testutils.NotSwallowedException;
import org.springframework.transaction.annotation.Transactional;

@RunWith(MustNotSwallowSpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/dbunit-context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, MustNoSwallowTestExecutionListener.class })
@Transactional
public class DoesNotSwallowExpectedFailureTest {

	@Test
	@ExpectedDatabase("/META-INF/db/expectedfail.xml")
	public void test() throws Exception {
		throw new NotSwallowedException();
	}

}
