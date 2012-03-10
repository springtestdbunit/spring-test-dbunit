package example.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import example.entity.Person;

@Service
@Transactional
public class PersonService {

	@PersistenceContext
	private EntityManager entityManager;
	
	@SuppressWarnings("unchecked")
	public List<Person> find(String name) {
		Query query = entityManager.createNamedQuery("Person.find");
		query.setParameter("name", "%"+name+"%");
		return query.getResultList();
	}
	
	public void remove(int personId) {
		Person person = entityManager.find(Person.class, personId);
		entityManager.remove(person);
	}
}
