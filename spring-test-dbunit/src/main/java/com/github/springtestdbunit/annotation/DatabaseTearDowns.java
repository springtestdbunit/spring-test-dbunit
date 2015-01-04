package com.github.springtestdbunit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container for repeating {@link DatabaseTearDown} annotations.
 *
 * @author Phillip Webb
 * @see DatabaseTearDown
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface DatabaseTearDowns {

	/**
	 * The {@link DatabaseTearDown} annotations to apply.
	 * @return the {@link DatabaseTearDown} annotations
	 */
	DatabaseTearDown[] value();

}
