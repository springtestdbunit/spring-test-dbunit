package org.springframework.test.dbunit.context;

import java.lang.reflect.Method;

import org.dbunit.database.IDatabaseConnection;
import org.springframework.test.dbunit.DataSetLoader;

public interface DbUnitTestContext {


	IDatabaseConnection getConnection();

	DataSetLoader getDataSetLoader();

	Class<?> getTestClass();

	Method getTestMethod();

}
