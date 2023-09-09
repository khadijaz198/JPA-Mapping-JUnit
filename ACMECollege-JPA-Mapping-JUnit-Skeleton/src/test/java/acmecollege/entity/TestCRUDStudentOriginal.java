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
public class TestCRUDStudentOriginal extends JUnitBase {

	private EntityManager em;
	private EntityTransaction et;


	private static Student student;


	
	@BeforeAll
	static void setupAllInit() {

	}

	@BeforeEach
	void setup() {
		em = getEntityManager();
		et = em.getTransaction();
		JUnitBase.deleteAllData();
	}

	@AfterEach
	void tearDown() {
		em.close();
	}

	@Test
	void test01_Empty() {
		assertThat(JUnitBase.getTotalCount(em, Student.class), is(comparesEqualTo(0L)));
	}

	@Test
	void test02_Create() {
		et.begin();
		student = new Student();
		student.setFullName("John", "Smith");
		em.persist(student);
		et.commit();

		long result = JUnitBase.getTotalCount(em, Student.class);

		// There should only be one row in the DB
		assertThat(result, is(greaterThanOrEqualTo(1L)));
	}

	@Test
	void test03_CreateInvalid() {
		et.begin();
		student = new Student();
		student.setFirstName("John");
		// We expect a failure because last name is not nullable
		assertThrows(PersistenceException.class, () -> em.persist(student));
		et.commit();
	}

	@Test
	void test04_Read() {
		
		et.begin();
		student = new Student();
		student.setFullName("John", "Smith");
		em.persist(student);
		et.commit();

		assertThat( JUnitBase.getAll(em, Student.class), contains(equalTo(student)));
	}

	@Test
	void test05_ReadDependencies() {
		
		et.begin();
		student = new Student();
		student.setFullName("John", "Smith");
		em.persist(student);
		Professor professor = new Professor();
		professor.setProfessor("Teddy", "Yap", "Information and Communications Technology");
		Course course1 = new Course();
		course1.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);
		DurationAndStatus ds = new DurationAndStatus();
		ds.setDurationAndStatus(LocalDateTime.of(2022, 8, 28, 0, 0), LocalDateTime.of(2023, 8, 27, 0, 0) , "+");
		StudentClub clubAcademic = new AcademicStudentClub();
		clubAcademic.setName("Computer Programming Club");
		em.persist(clubAcademic);
		CourseRegistration cr1 = new CourseRegistration();
		cr1.setProfessor(professor);
		cr1.setCourse(course1);
		cr1.setLetterGrade("A+");
		cr1.setNumericGrade(100);
		cr1.setStudent(student);
		em.persist(cr1);
		ClubMembership membership = new ClubMembership();
		membership.setDurationAndStatus(ds);
		membership.setStudentClub(clubAcademic);
		em.persist(membership);
		MembershipCard card = new MembershipCard();
		card.setOwner(student);
		card.setSigned(true);
		card.setClubMembership(membership);
		em.persist(card);
		et.commit();
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Student> query = builder.createQuery(Student.class);
		Root<Student> root = query.from(Student.class);
		query.select(root);
		query.where(builder.equal(root.get(Student_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Student> tq = em.createQuery(query);
		tq.setParameter("id", student.getId());
		// Get the result as row count
		Student returnedStudent = tq.getSingleResult();

		assertThat(returnedStudent.getFirstName(), equalTo("John"));
		assertThat(returnedStudent.getLastName(), equalTo("Smith"));
		assertThat(returnedStudent.getMembershipCards(), contains(equalTo(card)));
		assertThat(returnedStudent.getCourseRegistrations(), contains(equalTo(cr1)));
	}

	@Test
	void test06_Update() {
		
		et.begin();
		student = new Student();
		student.setFullName("John", "Smith");
		em.persist(student);
		et.commit();

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Student> query = builder.createQuery(Student.class);
		Root<Student> root = query.from(Student.class);
		query.select(root);
		query.where(builder.equal(root.get(Student_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Student> tq = em.createQuery(query);
		tq.setParameter("id", student.getId());
		// Get the result as row count
		Student returnedStudent = tq.getSingleResult();

		String newFirstName = "Stephanie";
		String newLastName = "Ngo";
		
		et.begin();
		returnedStudent.setFirstName(newFirstName);
		returnedStudent.setLastName(newLastName);
		em.merge(returnedStudent);
		et.commit();

		returnedStudent = tq.getSingleResult();

		assertThat(returnedStudent.getFirstName(), equalTo(newFirstName));
		assertThat(returnedStudent.getLastName(), equalTo(newLastName));
	}

	@Test
	void test07_UpdateDependencies() {
		
		et.begin();
		student = new Student();
		student.setFullName("John", "Smith");
		em.persist(student);
		Professor professor = new Professor();
		professor.setProfessor("Teddy", "Yap", "Information and Communications Technology");
		Course course1 = new Course();
		course1.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);
		DurationAndStatus ds = new DurationAndStatus();
		ds.setDurationAndStatus(LocalDateTime.of(2022, 8, 28, 0, 0), LocalDateTime.of(2023, 8, 27, 0, 0) , "+");
		StudentClub clubAcademic = new AcademicStudentClub();
		clubAcademic.setName("Computer Programming Club");
		em.persist(clubAcademic);
		CourseRegistration cr1 = new CourseRegistration();
		cr1.setProfessor(professor);
		cr1.setCourse(course1);
		cr1.setLetterGrade("A+");
		cr1.setNumericGrade(100);
		cr1.setStudent(student);
		em.persist(cr1);
		ClubMembership membership = new ClubMembership();
		membership.setDurationAndStatus(ds);
		membership.setStudentClub(clubAcademic);
		em.persist(membership);
		MembershipCard card = new MembershipCard();
		card.setOwner(student);
		card.setSigned(true);
		card.setClubMembership(membership);
		em.persist(card);
		et.commit();
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Student> query = builder.createQuery(Student.class);
		Root<Student> root = query.from(Student.class);
		query.select(root);
		query.where(builder.equal(root.get(Student_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Student> tq = em.createQuery(query);
		tq.setParameter("id", student.getId());
		// Get the result as row count
		Student returnedStudent = tq.getSingleResult();
		
		et.begin();
		Professor professor2 = new Professor();
		professor2.setProfessor("Charles", "Xavier", "Physics");
		Course course2 = new Course();
		course2.setCourse("CST8116", "Introduction to Computer Programming", 2021, "WINTER", 3, (byte) 0);
		DurationAndStatus ds2 = new DurationAndStatus();
		ds2.setDurationAndStatus(LocalDateTime.of(2022, 8, 28, 0, 0), LocalDateTime.of(2023, 8, 27, 0, 0) , "+");
		StudentClub clubNonAcademic = new NonAcademicStudentClub();
		clubNonAcademic.setName("Student Hiking Club");
		em.persist(clubNonAcademic);
		CourseRegistration cr2 = new CourseRegistration();
		cr2.setProfessor(professor2);
		cr2.setCourse(course2);
		cr2.setLetterGrade("A+");
		cr2.setNumericGrade(100);
		cr2.setStudent(student);
		em.persist(cr2);
		ClubMembership membership2 = new ClubMembership();
		membership2.setDurationAndStatus(ds2);
		membership2.setStudentClub(clubNonAcademic);
		em.persist(membership2);
		MembershipCard card2 = new MembershipCard();
		card2.setOwner(student);
		card2.setSigned(true);
		card2.setClubMembership(membership2);
		em.persist(card2);
		
		et.commit();

		returnedStudent = tq.getSingleResult();

		assertTrue(returnedStudent.getMembershipCards().size()==2);
		assertTrue(returnedStudent.getCourseRegistrations().size()==2);
	}

	@Test
	void test08_Delete() {
		
		et.begin();
		student = new Student();
		student.setFullName("John", "Smith");
		em.persist(student);
		et.commit();

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Student> query = builder.createQuery(Student.class);
		Root<Student> root = query.from(Student.class);
		query.select(root);
		query.where(builder.equal(root.get(Student_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Student> tq = em.createQuery(query);
		tq.setParameter("id", student.getId());
		// Get the result as row count
		Student returnedStudent = tq.getSingleResult();

		et.begin();
		Student student2 = new Student();
		student2.setFullName("Jack", "Jackson");
		em.persist(student2);
		et.commit();

		et.begin();
		em.remove(returnedStudent);
		et.commit();

		CriteriaQuery<Long> query2 = builder.createQuery(Long.class);
		Root<Student> root2 = query2.from(Student.class);
		query2.select(builder.count(root2));

		query2.where(builder.equal(root2.get(Student_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Long> tq2 = em.createQuery(query2);
		tq2.setParameter("id", returnedStudent.getId());
		long result = tq2.getSingleResult();
		assertThat(result, is(equalTo(0L)));

		// Create query and set the parameter
		TypedQuery<Long> tq3 = em.createQuery(query2);
		tq3.setParameter("id", student2.getId());
		// Get the result as row count
		result = tq3.getSingleResult();
		assertThat(result, is(equalTo(1L)));
	}
}

