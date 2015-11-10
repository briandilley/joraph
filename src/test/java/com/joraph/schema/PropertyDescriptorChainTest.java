package com.joraph.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PropertyDescriptorChainTest {

	@Test
	public void testReadRootLevelItem()
			throws Exception {

		Person person = new Person();

		PropertyDescriptorChain chain = new PropertyDescriptorChain.Builder()
				.addAccessor(Person::getId)
				.build();

		assertNull(chain.read(person, false));
		assertNull(chain.read(person, true));

		person.setId("an id");
		assertEquals("an id", chain.read(person , false));
		assertEquals("an id", chain.read(person , true));
	}

	@Test(expected=IllegalStateException.class)
	public void testReadSubItemsFailsOnNullsWhenToldTo()
			throws Exception {

		Person megatron = new Person();

		PropertyDescriptorChain firstNameChain = new PropertyDescriptorChain.Builder()
				.addAccessor(Person::getName)
				.addAccessor(Name::getFirstName)
				.build();

		firstNameChain.read(megatron, true);
	}

	@Test
	public void testReadSubItemsDoesNotFailOnNullsWhenToldNotTo()
			throws Exception {

		Person megatron = new Person();

		PropertyDescriptorChain firstNameChain = new PropertyDescriptorChain.Builder()
				.addAccessor(Person::getName)
				.addAccessor(Name::getFirstName)
				.build();

		assertNull(firstNameChain.read(megatron, false));
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
		


		PropertyDescriptorChain firstNameChain = new PropertyDescriptorChain.Builder()
				.addAccessor(Person::getName)
				.addAccessor(Name::getFirstName)
				.build();


		PropertyDescriptorChain friendLastNameChain = new PropertyDescriptorChain.Builder()
				.addAccessor(Person::getFriend)
				.addAccessor(Person::getName)
				.addAccessor(Name::getLastName)
				.build();

		assertEquals(megatron.getName().getFirstName(), firstNameChain.read(megatron, true));
		assertEquals(megatron.getName().getFirstName(), firstNameChain.read(megatron, true));
		assertEquals(megatron.getName().getLastName(), friendLastNameChain.read(starscream, true));
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
