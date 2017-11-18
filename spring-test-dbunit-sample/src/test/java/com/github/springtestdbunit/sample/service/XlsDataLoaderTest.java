package com.github.springtestdbunit.sample.service;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.dataset.XlsDataSetLoader;
import com.github.springtestdbunit.sample.entity.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:com/github/springtestdbunit/sample/service/applicationContext.xml"})
@DbUnitConfiguration(dataSetLoader= XlsDataSetLoader.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class XlsDataLoaderTest {

	@Autowired
	private PersonService personService;

	@Test
	@DatabaseSetup("sampleData.xls")
	public void testFind() throws Exception {
		List<Person> personList = this.personService.find("wang");
		assertEquals(1, personList.size());
		assertEquals("wu", personList.get(0).getLastName());
	}

}
