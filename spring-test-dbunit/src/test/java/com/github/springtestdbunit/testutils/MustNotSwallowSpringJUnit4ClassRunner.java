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

package com.github.springtestdbunit.testutils;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Special {@link SpringJUnit4ClassRunner} that ensures {@link NotSwallowedException} gets reported.
 * 
 * @author Phillip Webb
 */
public class MustNotSwallowSpringJUnit4ClassRunner extends SpringJUnit4ClassRunner {

	public MustNotSwallowSpringJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
	}

	@Override
	public void run(RunNotifier notifier) {
		AdvisedSupport config = new AdvisedSupport();
		config.setTarget(notifier);
		config.addAdvice(new org.aopalliance.intercept.MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				if ("fireTestFailure".equals(invocation.getMethod().getName())) {
					Failure failure = (Failure) invocation.getArguments()[0];
					if (failure.getException() instanceof NotSwallowedException) {
						// We expect this
						return null;
					}
				}
				return invocation.proceed();
			}
		});
		DefaultAopProxyFactory aopProxyFactory = new DefaultAopProxyFactory();
		RunNotifier runNotifierProxy = (RunNotifier) aopProxyFactory.createAopProxy(config).getProxy();
		super.run(runNotifierProxy);
	}

}
