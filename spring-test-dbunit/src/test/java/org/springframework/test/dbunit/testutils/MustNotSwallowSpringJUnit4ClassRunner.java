package org.springframework.test.dbunit.testutils;

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
