package org.github.springtestdbunit.sample.service;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.github.springtestdbunit.DbUnitTestExecutionListener;
import org.github.springtestdbunit.annotation.DatabaseSetup;
import org.github.springtestdbunit.annotation.ExpectedDatabase;
import org.github.springtestdbunit.sample.entity.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class PersonServiceTest {

	@Autowired
	private PersonService personService;

	@Test
	@DatabaseSetup("sampleData.xml")
	public void testFind() throws Exception {
		List<Person> personList = this.personService.find("hil");
		assertEquals(1, personList.size());
		assertEquals("Phillip", personList.get(0).getFirstName());
	}

	@Test
	@DatabaseSetup("sampleData.xml")
	@ExpectedDatabase("expectedData.xml")
	public void testRemove() throws Exception {
		this.personService.remove(1);
	}

}
