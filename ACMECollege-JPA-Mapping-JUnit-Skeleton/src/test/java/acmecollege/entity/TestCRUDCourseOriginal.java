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
public class TestCRUDCourseOriginal extends JUnitBase {

	private EntityManager em;
	private EntityTransaction et;

	private static Course course;

	@BeforeAll
	static void setupAllInit() {

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
		assertThat(JUnitBase.getTotalCount(em, Course.class), is(comparesEqualTo(0L)));
	}

	@Test
	void test02_Create() {
		et.begin();
		course = new Course();
		course.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);
		em.persist(course);
		et.commit();
		long result = JUnitBase.getTotalCount(em, Course.class);

		// There should only be one row in the DB
		assertThat(result, is(greaterThanOrEqualTo(1L)));
	}

	@Test
	void test03_CreateInvalid() {
		et.begin();
		course = new Course();
		course.setCourseCode("CST8277");
		assertThrows(PersistenceException.class, () -> em.persist(course));
		et.commit();
	}

	@Test
	void test04_Read() {
		
		et.begin();
		course = new Course();
		course.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);
		em.persist(course);
		et.commit();

		assertThat( JUnitBase.getAll(em, Course.class), contains(equalTo(course)));
	}


	@Test
	void test05_Update() {
		
		et.begin();
		course = new Course();
		course.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);
		em.persist(course);
		et.commit();

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Course> query = builder.createQuery(Course.class);
		Root<Course> root = query.from(Course.class);
		query.select(root);
		query.where(builder.equal(root.get(Course_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Course> tq = em.createQuery(query);
		tq.setParameter("id", course.getId());
		// Get the result as row count
		Course returnedCourse = tq.getSingleResult();

		String newCourseCode = "CST8284";
		String newCourseName = "Object-Oriented Programming in Java";
		
		et.begin();
		returnedCourse.setCourseCode(newCourseCode);
		returnedCourse.setCourseTitle(newCourseName);
		em.merge(returnedCourse);
		et.commit();

		returnedCourse = tq.getSingleResult();

		assertThat(returnedCourse.getCourseCode(), equalTo(newCourseCode));
		assertThat(returnedCourse.getCourseTitle(), equalTo(newCourseName));
	}


	@Test
	void test06_Delete() {
		
		et.begin();
		course = new Course();
		course.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);
		em.persist(course);
		et.commit();
	

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Course> query = builder.createQuery(Course.class);
		Root<Course> root = query.from(Course.class);
		query.select(root);
		query.where(builder.equal(root.get(Course_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Course> tq = em.createQuery(query);
		tq.setParameter("id", course.getId());
		// Get the result as row count
		Course returnedCourse = tq.getSingleResult();

		et.begin();
		Course course2 = new Course();
		course2.setCourse("CST8284", "Object-Oriented Programming in Java", 2022, "SUMMER", 3, (byte) 1);
		em.persist(course2);
		et.commit();

		et.begin();
		em.remove(returnedCourse);
		et.commit();

		CriteriaQuery<Long> query2 = builder.createQuery(Long.class);
		Root<Course> root2 = query2.from(Course.class);
		query2.select(builder.count(root2));

		query2.where(builder.equal(root2.get(Course_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Long> tq2 = em.createQuery(query2);
		tq2.setParameter("id", returnedCourse.getId());
		long result = tq2.getSingleResult();
		assertThat(result, is(equalTo(0L)));

		// Create query and set the parameter
		TypedQuery<Long> tq3 = em.createQuery(query2);
		tq3.setParameter("id", course2.getId());
		// Get the result as row count
		result = tq3.getSingleResult();
		assertThat(result, is(equalTo(1L)));
	}
}

