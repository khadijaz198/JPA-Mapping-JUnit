package common;

import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import acmecollege.entity.ClubMembership;
import acmecollege.entity.Course;
import acmecollege.entity.CourseRegistration;
import acmecollege.entity.CourseRegistrationPK;
import acmecollege.entity.CourseRegistration_;
import acmecollege.entity.MembershipCard;
import acmecollege.entity.Professor;
import acmecollege.entity.Student;
import acmecollege.entity.StudentClub;
import acmecollege.entity.Student_;

/**
 * Super class for all JUnit tests, holds common methods for creating {@link EntityManagerFactory} and truncating the DB
 * before all.
 * 
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @version August 28, 2022
 */
public class JUnitBase {

	protected static final Logger LOG = LogManager.getLogger();

	/**
	 * Default name of Persistence Unit = "acmecollege-PU"
	 */
	private static final String PERSISTENCE_UNIT = "acmecollege-PU";

	/**
	 * Static instance of {@link EntityManagerFactory} for subclasses
	 */
	protected static EntityManagerFactory emf;

	/**
	 * Create an instance of {@link EntityManagerFactory} using {@link JUnitBase#PERSISTENCE_UNIT}.<br>
	 * redirects to {@link JUnitBase#buildEMF(String)}.
	 * 
	 * @return An instance of EntityManagerFactory
	 */
	protected static EntityManagerFactory buildEMF() {
		return buildEMF(PERSISTENCE_UNIT);
	}

	/**
	 * Create an instance of {@link EntityManagerFactory} using provided Persistence Unit name.
	 * 
	 * @return An instance of EntityManagerFactory
	 */
	protected static EntityManagerFactory buildEMF(String persistenceUnitName) {
		Objects.requireNonNull(persistenceUnitName, "Persistence Unit name cannot be null");
		if (persistenceUnitName.isBlank()) {
			throw new IllegalArgumentException("Persistence Unit name cannot be empty or just white space");
		}
		return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
	}

	/**
	 * Create a new instance of {@link EntityManager}.<br>
	 * must call {@link JUnitBase#buildEMF()} or {@link JUnitBase#buildEMF(String)} first.
	 * 
	 * @return An instance of {@link EntityManager}
	 */
	protected static EntityManager getEntityManager() {
		if (emf == null) {
			throw new IllegalStateException("EntityManagerFactory is null, must call JUnitBase::buildEMF first");
		}
		return emf.createEntityManager();
	}

	/**
	 * Delete all Entities.  Order of delete matters.
	 */
	protected static void deleteAllData() {
		EntityManager em = getEntityManager();

		// Done JB01 - Begin transaction and truncate all tables.  Order matters.
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaDelete<CourseRegistration> q1 = cb.createCriteriaDelete(CourseRegistration.class);
		q1.from(CourseRegistration.class);
		CriteriaDelete<Course> q2 = cb.createCriteriaDelete(Course.class);
		q2.from(Course.class);
		CriteriaDelete<Professor> q3 = cb.createCriteriaDelete(Professor.class);
		q3.from(Professor.class);
		CriteriaDelete<MembershipCard> q4 = cb.createCriteriaDelete(MembershipCard.class);
		q4.from(MembershipCard.class);
		CriteriaDelete<ClubMembership> q5 = cb.createCriteriaDelete(ClubMembership.class);
		q5.from(ClubMembership.class);
		CriteriaDelete<StudentClub> q6 = cb.createCriteriaDelete(StudentClub.class);
		q6.from(StudentClub.class);
		CriteriaDelete<Student> q7 = cb.createCriteriaDelete(Student.class);
		q7.from(Student.class);

		EntityTransaction et = em.getTransaction();
		et.begin();
		em.createQuery(q1).executeUpdate();
		em.createQuery(q2).executeUpdate();
		em.createQuery(q3).executeUpdate();
		em.createQuery(q4).executeUpdate();
		em.createQuery(q5).executeUpdate();
		em.createQuery(q6).executeUpdate();
		em.createQuery(q7).executeUpdate();
		et.commit();
		
		em.close();

	}

	/**
	 * Delete all instances of provided type form the DB.  Same operation as truncate.
	 * 
	 * @see <a href = "https://stackoverflow.com/questions/23269885/truncate-delete-from-given-the-entity-class">
	 *      StackOverflow: Truncate with JPA</a>
	 * @param <T>        - Type of entity to delete, can be inferred by JVM when method is being executed.
	 * @param entityType - Class type of entity, like Professor.class
	 * @param em         - EntityManager to be used
	 * @return The number of entities updated or deleted
	 */
	public static <T> int deleteAllFrom(Class<T> entityType, EntityManager em) {
		// Done JB02 - Using CriteriaBuilder create a CriteriaDelete to execute a truncate on DB.
	    CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaDelete<T> query = builder.createCriteriaDelete(entityType);
	    query.from(entityType);
	    return em.createQuery(query).executeUpdate();
		//return -1;
	}

	protected static <T> long getTotalCount(EntityManager em, Class<T> clazz) {
		// Done JB03 - Optional helper method.  Create a CriteriaQuery here to be reused in your tests.
		// Method signature is just a suggestion it can be modified if need be.
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<T> root = query.from(clazz);
		query.select(builder.count(root));
		TypedQuery<Long> tq = em.createQuery(query);
		long result = tq.getSingleResult();
		return result;
	}

	protected static <T> List<T> getAll(EntityManager em, Class<T> clazz) {
		// Done JB04 - Optional helper method.  Create a CriteriaQuery here to be reused in your tests.
		// Method signature is just a suggestion it can be modified if need be.
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<T> query = builder.createQuery(clazz);
		Root<T> root = query.from(clazz);
		query.select(root);
		TypedQuery<T> tq = em.createQuery(query);
		return tq.getResultList();
	}

	protected static <T, R> T getWithId(EntityManager em, Class<T> clazz, Class<R> classPK,
			SingularAttribute<? super T, R> sa, R id) {
	

//		CriteriaBuilder builder = em.getCriteriaBuilder();
//		CriteriaQuery<T> query = builder.createQuery(clazz);
//		Root<T> root = query.from(clazz);
//		query.select(root);

		// TODO JB05 - Optional helper method.  Create a CriteriaQuery here to be reused in your tests.
		// Method signature is just a suggestion it can be modified if need be.
		return null;
	}

	protected static <T, R> long getCountWithId(EntityManager em, Class<T> clazz, Class< R> classPK,
			SingularAttribute<? super T, R> sa, R id) {
		// TODO JB06 - Optional helper method.  Create a CriteriaQuery here to be reused in your tests.
		// Method signature is just a suggestion it can be modified if need be.
		return -1;
	}

	@BeforeAll
	static void setupAll() {
		emf = buildEMF();
		deleteAllData();
	}

	@AfterAll
	static void tearDownAll() {
		emf.close();
	}
}
