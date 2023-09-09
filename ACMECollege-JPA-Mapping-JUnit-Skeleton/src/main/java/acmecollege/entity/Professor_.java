package acmecollege.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2022-11-06T19:47:39.678-0500")
@StaticMetamodel(Professor.class)
public class Professor_ extends PojoBase_ {
	public static volatile SingularAttribute<Professor, String> firstName;
	public static volatile SingularAttribute<Professor, String> lastName;
	public static volatile SingularAttribute<Professor, String> department;
	public static volatile SetAttribute<Professor, CourseRegistration> courseRegistrations;
}
