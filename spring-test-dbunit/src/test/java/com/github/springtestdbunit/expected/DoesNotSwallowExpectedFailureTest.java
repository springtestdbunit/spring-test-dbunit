package com.github.springtestdbunit.expected;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.testutils.MustNoSwallowTestExecutionListener;
import com.github.springtestdbunit.testutils.MustNotSwallowSpringJUnit4ClassRunner;
import com.github.springtestdbunit.testutils.NotSwallowedException;

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
