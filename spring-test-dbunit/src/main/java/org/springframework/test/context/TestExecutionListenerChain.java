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
package org.springframework.test.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * A {@link TestExecutionListener} implementation that works by chaining together other {@link TestExecutionListener}s
 * and ensures that methods are called in the correct order. The {@link #prepareTestInstance}, {@link #beforeTestClass}
 * and {@link #afterTestClass} methods will delegate to the chain in the order that it is defined. The
 * {@link #afterTestClass} and {@link #afterTestMethod} methods will delegate in reverse order. methods.
 * <p>
 * For example, a typical call on a chain containing items "a" and "b" would be: <code>a.beforeTestMethod</code>,
 * <code>b.beforeTestMethod</code>, <code>b.afterTestMethod</code>, <code>a.afterTestMethod</code>.
 * 
 * @author Phillip Webb
 */
public abstract class TestExecutionListenerChain implements TestExecutionListener {

	private List<TestExecutionListener> chain;
	private List<TestExecutionListener> reverseChain;

	public TestExecutionListenerChain() {
		chain = createChain();
		reverseChain = new ArrayList<TestExecutionListener>(chain);
		Collections.reverse(reverseChain);
	}

	/**
	 * Returns the chain of {@link TestExecutionListener} classes in the correct order.
	 * @return The chain
	 */
	protected abstract Class<?>[] getChain();

	/**
	 * Factory method used to create the chain. By default this method will construct the chain using the classes from
	 * {@link #getChain()}.
	 * @return The chain
	 */
	protected List<TestExecutionListener> createChain() {
		Class<?>[] chainClasses = getChain();
		try {
			List<TestExecutionListener> chain = new ArrayList<TestExecutionListener>(chainClasses.length);
			for (int i = 0; i < chainClasses.length; i++) {
				chain.add((TestExecutionListener) chainClasses[i].newInstance());
			}
			return chain;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to create chain for classes " + Arrays.asList(chainClasses), e);
		}
	}

	public void beforeTestClass(final TestContext testContext) throws Exception {
		forwards(new Call() {
			public void call(TestExecutionListener listener) throws Exception {
				listener.beforeTestClass(testContext);
			}
		});
	}

	public void prepareTestInstance(final TestContext testContext) throws Exception {
		forwards(new Call() {
			public void call(TestExecutionListener listener) throws Exception {
				listener.prepareTestInstance(testContext);
			}
		});
	}

	public void beforeTestMethod(final TestContext testContext) throws Exception {
		forwards(new Call() {
			public void call(TestExecutionListener listener) throws Exception {
				listener.beforeTestMethod(testContext);
			}
		});
	}

	public void afterTestMethod(final TestContext testContext) throws Exception {
		backwards(new Call() {
			public void call(TestExecutionListener listener) throws Exception {
				listener.afterTestMethod(testContext);
			}
		});
	}

	public void afterTestClass(final TestContext testContext) throws Exception {
		backwards(new Call() {
			public void call(TestExecutionListener listener) throws Exception {
				listener.afterTestClass(testContext);
			}
		});
	}

	private void forwards(Call call) throws Exception {
		runChain(chain.iterator(), call);
	}

	private void backwards(Call call) throws Exception {
		runChain(reverseChain.iterator(), call);
	}

	private void runChain(Iterator<TestExecutionListener> iterator, Call call) throws Exception {
		Throwable ex = null;
		while (iterator.hasNext()) {
			try {
				call.call(iterator.next());
			} catch (Throwable e) {
				ex = e;
			}
		}
		if (ex != null) {
			if (ex instanceof Exception) {
				throw (Exception) ex;
			}
			throw new Exception(ex);
		}
	}

	private static interface Call {
		public void call(TestExecutionListener listener) throws Exception;
	}
}
