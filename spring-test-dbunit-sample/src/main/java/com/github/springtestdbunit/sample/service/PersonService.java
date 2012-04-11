package com.github.springtestdbunit.sample.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.sample.entity.Person;


@Service
@Transactional
public class PersonService {

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<Person> find(String name) {
		Query query = this.entityManager.createNamedQuery("Person.find");
		query.setParameter("name", "%" + name + "%");
		return query.getResultList();
	}

	public void remove(int personId) {
		Person person = this.entityManager.find(Person.class, personId);
		this.entityManager.remove(person);
	}
}
