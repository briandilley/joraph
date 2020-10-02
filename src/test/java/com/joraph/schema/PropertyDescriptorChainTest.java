package com.joraph.schema;

import static com.joraph.ChainableFunc.chain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.joraph.ChainableFunc;

public class PropertyDescriptorChainTest {

	@Test
	public void testReadRootLevelItem() {

		Person person = new Person();

		ChainableFunc<Person, String> chain = chain(Person::getId);

		assertNull(chain.read(person));
		assertNull(chain.read(person));

		person.setId("an id");
		assertEquals("an id", chain.read(person));
		assertEquals("an id", chain.read(person));
	}

	@Test
	public void testReadSubItemsDoesNotFailOnNullsWhenToldNotTo()
			throws Exception {

		Person megatron = new Person();

		ChainableFunc<Person, String> firstNameChain = chain(Person::getName)
				.andThen(Name::getFirstName);

		assertNull(firstNameChain.read(megatron));
	}

	@Test
	public void testReadSubItems()
			throws Exception {

		Person megatron = new Person();
		megatron.setId("megatron");
		megatron.setName(new Name());
		megatron.getName().setFirstName("Megatron");
		megatron.getName().setLastName("Smith");

		Person starscream = new Person();
		starscream.setId("starscream");
		starscream.setName(new Name());
		starscream.getName().setFirstName("Starscream");
		starscream.getName().setLastName("Patel");
		starscream.setFriend(megatron);

		ChainableFunc<Person, String> firstNameChain = chain(Person::getName)
				.andThen(Name::getFirstName);

		ChainableFunc<Person, String> friendLastNameChain = chain(Person::getFriend)
				.andThen(Person::getName)
				.andThen(Name::getLastName);

		assertEquals(megatron.getName().getFirstName(), firstNameChain.read(megatron));
		assertEquals(megatron.getName().getFirstName(), firstNameChain.read(megatron));
		assertEquals(megatron.getName().getLastName(), friendLastNameChain.read(starscream));
	}

	public class Person {
		private String id;
		private Name name;
		private Person friend;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public Name getName() {
			return name;
		}
		public void setName(Name name) {
			this.name = name;
		}
		public Person getFriend() {
			return friend;
		}
		public void setFriend(Person friend) {
			this.friend = friend;
		}
	}

	public class Name {
		private String firstName;
		private String lastName;
		public String getFirstName() {
			return firstName;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public String getLastName() {
			return lastName;
		}
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}

}
