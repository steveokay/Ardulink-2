package org.ardulink.core.beans;

import static org.ardulink.core.beans.finder.impl.FindByAnnotation.propertyAnnotated;
import static org.ardulink.core.beans.finder.impl.FindByFieldAccess.directFieldAccess;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import org.junit.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class AnnotationsTest {

	@Retention(RUNTIME)
	@interface AnotherAnno {
	}

	@Retention(RUNTIME)
	public @interface SomeAnno {
		String value() default "";
	}

	class AnotherAnnoOnTheField {
		@SomeAnno
		@AnotherAnno
		public String string;
	}

	class AnotherAnnoOnTheFieldWithGetterAndSetter {
		@SomeAnno("string")
		@AnotherAnno
		private String string;

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}

	}

	class AnotherAnnoOnTheGetter {
		private String string;

		@SomeAnno
		@AnotherAnno
		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}
	}

	class AnotherAnnoOnTheSetter {
		private String string;

		public String getString() {
			return string;
		}

		@SomeAnno
		@AnotherAnno
		public void setString(String string) {
			this.string = string;
		}
	}

	class AnotherAnnoOnTheGetterAndSetter {
		private String string;

		@SomeAnno
		@AnotherAnno
		public String getString() {
			return string;
		}

		@SomeAnno
		@AnotherAnno
		public void setString(String string) {
			this.string = string;
		}
	}

	@Test
	public void anotherAnnoOnTheField() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.builder(new AnotherAnnoOnTheField())
				.using(directFieldAccess()).build());
	}

	@Test
	public void anotherAnnoOnTheFieldWithGetterAndSetter() throws Exception {
		// this will only work if the property was found using propertyAnnotated
		// since when looking up via findByIntrospection their is no relation
		// between the reader/setter and the private field!
		assertHasBothAnnotations(BeanProperties
				.builder(new AnotherAnnoOnTheFieldWithGetterAndSetter())
				.using(propertyAnnotated(SomeAnno.class)).build());
	}

	@Test
	public void testAnnoOnGetter() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.forBean(new AnotherAnnoOnTheGetter()));
	}

	@Test
	public void testAnnoOnSetter() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.forBean(new AnotherAnnoOnTheSetter()));
	}

	@Test
	public void testAnnoOnGetterAndSetter() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.forBean(new AnotherAnnoOnTheGetterAndSetter()));
	}

	@SuppressWarnings("unchecked")
	private void assertHasBothAnnotations(BeanProperties beanProperties) {
		hasAnnotations(
				checkNotNull(beanProperties.getAttribute("string"),
						"no attribute named \"string\" found in %s",
						beanProperties), SomeAnno.class, AnotherAnno.class);
	}

	private void hasAnnotations(Attribute attribute,
			final Class<? extends Annotation>... annoClasses) {
		for (int i = 0; i < annoClasses.length; i++) {
			assertThat(annoClasses[i].getSimpleName() + " not found",
					attribute.getAnnotation(annoClasses[i]), is(notNullValue()));
		}
	}

}
