package acmecollege.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import common.JUnitBase;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestCRUDProfessorOriginal extends JUnitBase {

	private EntityManager em;
	private EntityTransaction et;

	private static Professor professor;
	private static String firstName;
	private static String lastName;
	private static String department;


	
	@BeforeAll
	static void setupAllInit() {

		firstName = "Charles";
		lastName = "Xavier";
		department = "Physics";

		
	}

	@BeforeEach
	void setup() {
		em = getEntityManager();
		et = em.getTransaction();
	}

	@AfterEach
	void tearDown() {
		JUnitBase.deleteAllData();
		em.close();
	}

	@Test
	void test01_Empty() {
		assertThat(JUnitBase.getTotalCount(em, Professor.class), is(comparesEqualTo(0L)));
	}

	@Test
	void test02_Create() {
		et.begin();
		professor = new Professor();
		professor.setProfessor(firstName, lastName, department);
		em.persist(professor);
		et.commit();

		long result = JUnitBase.getTotalCount(em, Professor.class);

		// There should only be one row in the DB
		assertThat(result, is(greaterThanOrEqualTo(1L)));
	}

	@Test
	void test03_CreateInvalid() {
		et.begin();
		professor = new Professor();
		professor.setFirstName(firstName);
		// We expect a failure because last name is not nullable
		assertThrows(PersistenceException.class, () -> em.persist(professor));
		et.commit();
	}

	@Test
	void test04_Read() {
		
		et.begin();
		professor = new Professor();
		professor.setProfessor(firstName, lastName, department);
		em.persist(professor);
		et.commit();

		assertThat( JUnitBase.getAll(em, Professor.class), contains(equalTo(professor)));
	}


	@Test
	void test05_Update() {
		
		et.begin();
		professor = new Professor();
		professor.setProfessor(firstName, lastName, department);
		em.persist(professor);
		et.commit();

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Professor> query = builder.createQuery(Professor.class);
		Root<Professor> root = query.from(Professor.class);
		query.select(root);
		query.where(builder.equal(root.get(Professor_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Professor> tq = em.createQuery(query);
		tq.setParameter("id", professor.getId());
		// Get the result as row count
		Professor returnedProf = tq.getSingleResult();

		String newFirstName = "Stephanie";
		String newLastName = "Ngo";
		
		et.begin();
		returnedProf.setFirstName(newFirstName);
		returnedProf.setLastName(newLastName);
		em.merge(returnedProf);
		et.commit();

		returnedProf = tq.getSingleResult();

		assertThat(returnedProf.getFirstName(), equalTo(newFirstName));
		assertThat(returnedProf.getLastName(), equalTo(newLastName));
	}


	@Test
	void test06_Delete() {
		
		et.begin();
		professor = new Professor();
		professor.setProfessor(firstName, lastName, department);
		em.persist(professor);
		et.commit();
	

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Professor> query = builder.createQuery(Professor.class);
		Root<Professor> root = query.from(Professor.class);
		query.select(root);
		query.where(builder.equal(root.get(Professor_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Professor> tq = em.createQuery(query);
		tq.setParameter("id", professor.getId());
		// Get the result as row count
		Professor returnedProf = tq.getSingleResult();

		et.begin();
		Professor professor2 = new Professor();
		professor2.setProfessor("Teddy", "Yap", "Information and Communications Technology");
		em.persist(professor2);
		et.commit();

		et.begin();
		em.remove(returnedProf);
		et.commit();

		CriteriaQuery<Long> query2 = builder.createQuery(Long.class);
		Root<Professor> root2 = query2.from(Professor.class);
		query2.select(builder.count(root2));

		query2.where(builder.equal(root2.get(Professor_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Long> tq2 = em.createQuery(query2);
		tq2.setParameter("id", returnedProf.getId());
		long result = tq2.getSingleResult();
		assertThat(result, is(equalTo(0L)));

		// Create query and set the parameter
		TypedQuery<Long> tq3 = em.createQuery(query2);
		tq3.setParameter("id", professor2.getId());
		// Get the result as row count
		result = tq3.getSingleResult();
		assertThat(result, is(equalTo(1L)));
	}
}

