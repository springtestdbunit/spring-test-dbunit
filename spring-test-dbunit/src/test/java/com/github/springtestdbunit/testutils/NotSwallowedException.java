package com.github.springtestdbunit.testutils;

/**
 * Exception that we can throw from a tests to make sure that the test is not swallowing failures.
 * 
 * @author Phillip Webb
 */
public class NotSwallowedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
}
