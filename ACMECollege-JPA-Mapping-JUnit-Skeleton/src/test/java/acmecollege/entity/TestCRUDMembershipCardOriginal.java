package acmecollege.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
public class TestCRUDMembershipCardOriginal extends JUnitBase {

	private EntityManager em;
	private EntityTransaction et;

	private static Course course;
	private static Professor professor;
	private static Student student;
	private static CourseRegistration courseRegistration;
	private static final String LETTER_GRADE = "A+";
	private static final int NUMERIC_GRADE = 100;
	private static ClubMembership membership;


	
	@BeforeAll
	static void setupAllInit() {
		student = new Student();
		student.setFullName("John", "Smith");
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
		assertThat(JUnitBase.getTotalCount(em, MembershipCard.class), is(comparesEqualTo(0L)));
	}

	@Test
	void test02_Create() {
		et.begin();

		StudentClub clubNonAcademic = new NonAcademicStudentClub();
		clubNonAcademic.setName("Student Hiking Club");
		em.persist(clubNonAcademic);

		Course course1 = new Course();
		course1.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);

		Student s = new Student();
		s.setFullName("John", "Smith");

		DurationAndStatus ds = new DurationAndStatus();
		ds.setDurationAndStatus(LocalDateTime.of(2022, 8, 28, 0, 0), LocalDateTime.of(2023, 8, 27, 0, 0) , "+");

		CourseRegistration cr1 = new CourseRegistration();
		cr1.setProfessor(professor);
		cr1.setCourse(course1);
		cr1.setLetterGrade("A+");
		cr1.setNumericGrade(100);
		cr1.setStudent(s);
		em.persist(cr1);

		ClubMembership membership = new ClubMembership();
		membership.setDurationAndStatus(ds);
		membership.setStudentClub(clubNonAcademic);
		em.persist(membership);

		MembershipCard card = new MembershipCard();
		card.setOwner(s);
		card.setSigned(true);
		card.setClubMembership(membership);
		em.persist(card);

		et.commit();

		long result = JUnitBase.getTotalCount(em, MembershipCard.class);

		assertThat(result, is(greaterThanOrEqualTo(1L)));
	}

	@Test
	void test03_CreateInvalid() {
		et.begin();
		MembershipCard card = new MembershipCard();
//		card.setOwner(student);
		card.setSigned(true);
		card.setClubMembership(membership);
		assertThrows(PersistenceException.class, () -> em.persist(card));
		et.commit();
	}

	@Test
	void test04_Read() {
		
		et.begin();

		StudentClub clubNonAcademic = new NonAcademicStudentClub();
		clubNonAcademic.setName("Student Hiking Club");
		em.persist(clubNonAcademic);

		Course course1 = new Course();
		course1.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);

		Student s = new Student();
		s.setFullName("John", "Smith");

		DurationAndStatus ds = new DurationAndStatus();
		ds.setDurationAndStatus(LocalDateTime.of(2022, 8, 28, 0, 0), LocalDateTime.of(2023, 8, 27, 0, 0) , "+");

		CourseRegistration cr1 = new CourseRegistration();
		cr1.setProfessor(professor);
		cr1.setCourse(course1);
		cr1.setLetterGrade("A+");
		cr1.setNumericGrade(100);
		cr1.setStudent(s);
		em.persist(cr1);

		ClubMembership membership = new ClubMembership();
		membership.setDurationAndStatus(ds);
		membership.setStudentClub(clubNonAcademic);
		em.persist(membership);

		MembershipCard card = new MembershipCard();
		card.setOwner(s);
		card.setSigned(true);
		card.setClubMembership(membership);
		em.persist(card);

		et.commit();
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<MembershipCard> query = builder.createQuery(MembershipCard.class);
		Root<MembershipCard> root = query.from(MembershipCard.class);
		query.select(root);
		TypedQuery<MembershipCard> tq = em.createQuery(query);
		List<MembershipCard> membershipCards = tq.getResultList();

		assertThat(membershipCards, contains(equalTo(card)));
	}

	@Test
	void test05_ReadDependencies() {
		et.begin();

		StudentClub clubNonAcademic = new NonAcademicStudentClub();
		clubNonAcademic.setName("Student Hiking Club");
		em.persist(clubNonAcademic);

		Course course1 = new Course();
		course1.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);

		Student s = new Student();
		s.setFullName("John", "Smith");

		DurationAndStatus ds = new DurationAndStatus();
		ds.setDurationAndStatus(LocalDateTime.of(2022, 8, 28, 0, 0), LocalDateTime.of(2023, 8, 27, 0, 0) , "+");

		CourseRegistration cr1 = new CourseRegistration();
		cr1.setProfessor(professor);
		cr1.setCourse(course1);
		cr1.setLetterGrade("A+");
		cr1.setNumericGrade(100);
		cr1.setStudent(s);
		em.persist(cr1);

		ClubMembership membership = new ClubMembership();
		membership.setDurationAndStatus(ds);
		membership.setStudentClub(clubNonAcademic);
		em.persist(membership);

		MembershipCard card = new MembershipCard();
		card.setOwner(s);
		card.setSigned(true);
		card.setClubMembership(membership);
		em.persist(card);

		et.commit();
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<MembershipCard> query = builder.createQuery(MembershipCard.class);
		Root<MembershipCard> root = query.from(MembershipCard.class);
		query.select(root);
		query.where(builder.equal(root.get(MembershipCard_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<MembershipCard> tq = em.createQuery(query);
		tq.setParameter("id", card.getId());
		// Get the result as row count
		MembershipCard returnedMembershipCard = tq.getSingleResult();

		assertThat(returnedMembershipCard.getClubMembership(), equalTo(membership));
		assertThat(returnedMembershipCard.getOwner(), equalTo(s));
		assertThat(returnedMembershipCard.getSigned(), equalTo((byte) 1));

	}

	@Test
	void test06_Update() {
		et.begin();

		StudentClub clubNonAcademic = new NonAcademicStudentClub();
		clubNonAcademic.setName("Student Hiking Club");
		em.persist(clubNonAcademic);

		Course course1 = new Course();
		course1.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);

		Student s = new Student();
		s.setFullName("John", "Smith");
	
		DurationAndStatus ds = new DurationAndStatus();
		ds.setDurationAndStatus(LocalDateTime.of(2022, 8, 28, 0, 0), LocalDateTime.of(2023, 8, 27, 0, 0) , "+");
	
		CourseRegistration cr1 = new CourseRegistration();
		cr1.setProfessor(professor);
		cr1.setCourse(course1);
		cr1.setLetterGrade("A+");
		cr1.setNumericGrade(100);
		cr1.setStudent(s);
		em.persist(cr1);

		ClubMembership membership = new ClubMembership();
		membership.setDurationAndStatus(ds);
		membership.setStudentClub(clubNonAcademic);
		em.persist(membership);


		MembershipCard card = new MembershipCard();
		card.setOwner(s);
		card.setSigned(true);
		card.setClubMembership(membership);
		em.persist(card);
	
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<MembershipCard> query = builder.createQuery(MembershipCard.class);
		Root<MembershipCard> root = query.from(MembershipCard.class);
		query.select(root);
		query.where(builder.equal(root.get(MembershipCard_.id), builder.parameter(Integer.class, "id")));
		TypedQuery<MembershipCard> tq = em.createQuery(query);
		tq.setParameter("id", card.getId());
		MembershipCard returnedMembershipCard = tq.getSingleResult();
	
		returnedMembershipCard.setSigned(false);

		em.merge(returnedMembershipCard);
		et.commit();

		returnedMembershipCard = tq.getSingleResult();

		assertThat(returnedMembershipCard.getSigned(), equalTo((byte) 0));
	}

	@Test
	void test07_Delete() {
		
		et.begin();

		StudentClub clubAcademic = new AcademicStudentClub();
		clubAcademic.setName("Computer Programming Club");
		em.persist(clubAcademic);

		StudentClub clubNonAcademic = new NonAcademicStudentClub();
		clubNonAcademic.setName("Student Hiking Club");
		em.persist(clubNonAcademic);

		Course course1 = new Course();
		course1.setCourse("CST8277", "Enterprise Application Programming", 2022, "AUTUMN", 3, (byte) 0);

		Course course2 = new Course();
		course2.setCourse("CST8284", "Object-Oriented Programming in Java", 2022, "SUMMER", 3, (byte) 1);

		Professor professor = new Professor();
		professor.setProfessor("Teddy", "Yap", "Information and Communications Technology");

		Student s = new Student();
		s.setFullName("John", "Smith");

		DurationAndStatus ds = new DurationAndStatus();
		ds.setDurationAndStatus(LocalDateTime.of(2022, 8, 28, 0, 0), LocalDateTime.of(2023, 8, 27, 0, 0) , "+");

		DurationAndStatus ds2 = new DurationAndStatus();
		ds2.setDurationAndStatus(LocalDateTime.of(2021, 1, 1, 0, 0), LocalDateTime.of(2021, 12, 31, 0, 0) , "-");

		CourseRegistration cr1 = new CourseRegistration();
		cr1.setProfessor(professor);
		cr1.setCourse(course1);
		cr1.setLetterGrade("A+");
		cr1.setNumericGrade(100);
		cr1.setStudent(s);
		em.persist(cr1);

		ClubMembership membership = new ClubMembership();
		membership.setDurationAndStatus(ds);
		membership.setStudentClub(clubNonAcademic);
		em.persist(membership);

		ClubMembership membership2 = new ClubMembership();
		membership2.setDurationAndStatus(ds2);
		membership2.setStudentClub(clubAcademic);
		em.persist(membership2);

		MembershipCard card = new MembershipCard();
		card.setOwner(s);
		card.setSigned(true);
		card.setClubMembership(membership);
		em.persist(card);

		MembershipCard card2 = new MembershipCard();
		card2.setOwner(s);
		card2.setSigned(false);
		card2.setClubMembership(membership);
		em.persist(card2);
		et.commit();

		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<MembershipCard> query = builder.createQuery(MembershipCard.class);
		Root<MembershipCard> root = query.from(MembershipCard.class);
		query.select(root);
		query.where(builder.equal(root.get(MembershipCard_.id), builder.parameter(Integer.class, "id")));
		TypedQuery<MembershipCard> tq = em.createQuery(query);
		tq.setParameter("id", card.getId());
		MembershipCard returnedMembershipCard = tq.getSingleResult();

		et.begin();
		em.remove(returnedMembershipCard);
		et.commit();

		// Create query for long as we need the number of found rows
		CriteriaQuery<Long> query2 = builder.createQuery(Long.class);
		// Select count(p) from Professor p where p.id = :id
		Root<MembershipCard> root2 = query2.from(MembershipCard.class);
		query2.select(builder.count(root2));
		query2.where(builder.equal(root2.get(MembershipCard_.id), builder.parameter(Integer.class, "id")));
		// Create query and set the parameter
		TypedQuery<Long> tq2 = em.createQuery(query2);
		tq2.setParameter("id", returnedMembershipCard.getId());
		// Get the result as row count
		long result = tq2.getSingleResult();
		assertThat(result, is(equalTo(0L)));

		// Create query and set the parameter
		TypedQuery<Long> tq3 = em.createQuery(query2);
		tq3.setParameter("id", card2.getId());
		// Get the result as row count
		result = tq3.getSingleResult();
		assertThat(result, is(equalTo(1L)));
	}
}

