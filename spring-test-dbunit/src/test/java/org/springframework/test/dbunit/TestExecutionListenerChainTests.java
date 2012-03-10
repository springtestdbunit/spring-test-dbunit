/*
 * Copyright 2010 the original author or authors
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
package org.springframework.test.dbunit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.dbunit.TestExecutionListenerChain;

/**
 * Tests for {@link TestExecutionListenerChain}.
 * 
 * @author Phillip Webb
 */
public class TestExecutionListenerChainTests {

	private InOrder ordered;
	private TestExecutionListenerChain chain;
	private TestContext testContext;
	private TestExecutionListener l1;
	private TestExecutionListener l2;

	@Before
	public void setup() {
		this.l1 = mock(TestExecutionListener.class);
		this.l2 = mock(TestExecutionListener.class);
		this.ordered = inOrder(this.l1, this.l2);
		this.chain = new TestExecutionListenerChain() {
			@Override
			protected Class<?>[] getChain() {
				return null;
			}

			@Override
			protected List<TestExecutionListener> createChain() {
				return Arrays.asList(TestExecutionListenerChainTests.this.l1, TestExecutionListenerChainTests.this.l2);
			};
		};
		this.testContext = mock(TestContext.class);
	}

	@Test
	public void shouldCreateChainFromClasses() throws Exception {
		this.chain = new TestExecutionListenerChain() {
			@Override
			protected Class<?>[] getChain() {
				return new Class<?>[] { TestListener1.class, TestListener2.class };
			};
		};
		List<TestExecutionListener> list = this.chain.createChain();
		assertEquals(2, list.size());
		assertTrue(list.get(0) instanceof TestListener1);
		assertTrue(list.get(1) instanceof TestListener2);
	}

	@Test
	public void shouldNotCreateWithIllegalConstructor() throws Exception {
		try {
			this.chain = new TestExecutionListenerChain() {
				@Override
				protected Class<?>[] getChain() {
					return new Class<?>[] { InvalidTestListener.class };
				};
			};
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Unable to create chain for classes [class org.springframework.test.context."
					+ "TestExecutionListenerChainTests$InvalidTestListener]", e.getMessage());
		}
	}

	@Test
	public void shouldChainBeforeTestClass() throws Exception {
		this.chain.beforeTestClass(this.testContext);
		this.ordered.verify(this.l1).beforeTestClass(this.testContext);
		this.ordered.verify(this.l2).beforeTestClass(this.testContext);
	}

	@Test
	public void shouldChainPrepareTestInstance() throws Exception {
		this.chain.prepareTestInstance(this.testContext);
		this.ordered.verify(this.l1).prepareTestInstance(this.testContext);
		this.ordered.verify(this.l2).prepareTestInstance(this.testContext);
	}

	@Test
	public void shouldChainBeforeTestMethod() throws Exception {
		this.chain.beforeTestMethod(this.testContext);
		this.ordered.verify(this.l1).beforeTestMethod(this.testContext);
		this.ordered.verify(this.l2).beforeTestMethod(this.testContext);
	}

	@Test
	public void shouldChainAfterTestMethod() throws Exception {
		this.chain.afterTestMethod(this.testContext);
		this.ordered.verify(this.l2).afterTestMethod(this.testContext);
		this.ordered.verify(this.l1).afterTestMethod(this.testContext);
	}

	@Test(expected = Exception.class)
	public void shouldChainAfterTestMethodEvenOnException() throws Exception {
		doThrow(new IOError(null)).when(this.l2).afterTestMethod(this.testContext);
		this.chain.afterTestMethod(this.testContext);
		this.ordered.verify(this.l2).afterTestMethod(this.testContext);
		this.ordered.verify(this.l1).afterTestMethod(this.testContext);
	}

	@Test
	public void shouldChainAfterTestClass() throws Exception {
		this.chain.afterTestClass(this.testContext);
		this.ordered.verify(this.l2).afterTestClass(this.testContext);
		this.ordered.verify(this.l1).afterTestClass(this.testContext);
	}

	@Test(expected = IOException.class)
	public void shouldChainAfterTestClassEvenOnException() throws Exception {
		doThrow(new IOException()).when(this.l2).afterTestClass(this.testContext);
		this.chain.afterTestClass(this.testContext);
		this.ordered.verify(this.l2).afterTestClass(this.testContext);
		this.ordered.verify(this.l1).afterTestClass(this.testContext);
	}

	public static class TestListener1 extends AbstractTestExecutionListener {

	}

	public static class TestListener2 extends AbstractTestExecutionListener {

	}

	public static class InvalidTestListener extends AbstractTestExecutionListener {
		public InvalidTestListener(String illegal) {
		}
	}

}
