package com.joraph.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.beans.IntrospectionException;

import org.junit.Test;

public class PropertyDescriptorChainTest {

	@Test(expected=IntrospectionException.class)
	public void testFailsOnInvalidProperty()
			throws Exception {
		new PropertyDescriptorChain("anus", Person.class);
	}

	@Test
	public void testReadRootLevelItem()
			throws Exception {

		Person person = new Person();

		PropertyDescriptorChain chain = new PropertyDescriptorChain("id", Person.class);

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

		PropertyDescriptorChain firstNameChain = new PropertyDescriptorChain("name.firstName", Person.class);
		firstNameChain.read(megatron, true);
	}

	@Test
	public void testReadSubItemsDoesNotFailOnNullsWhenToldNotTo()
			throws Exception {

		Person megatron = new Person();

		PropertyDescriptorChain firstNameChain = new PropertyDescriptorChain("name.firstName", Person.class);
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
		

		PropertyDescriptorChain firstNameChain = new PropertyDescriptorChain("name.firstName", Person.class);
		PropertyDescriptorChain friendLastNameChain = new PropertyDescriptorChain("friend.name.lastName", Person.class);

		assertEquals(megatron.getName().getFirstName(), firstNameChain.read(megatron, true));
		assertEquals(megatron.getName().getFirstName(), firstNameChain.read(megatron, true));
		assertEquals(megatron.getName().getLastName(), friendLastNameChain.read(starscream, true));
	}

	@Test
	public void testWriteRootLevelItem()
			throws Exception {

		Person person = new Person();

		PropertyDescriptorChain chain = new PropertyDescriptorChain("id", Person.class);

		chain.write(person, "an id", true);
		assertEquals("an id", person.getId());
	}

	@Test(expected=IllegalStateException.class)
	public void testWriteSubItemsFailsOnNullsWhenToldTo()
			throws Exception {

		Person megatron = new Person();

		PropertyDescriptorChain firstNameChain = new PropertyDescriptorChain("name.firstName", Person.class);
		firstNameChain.write(megatron, "Megatron", true);
	}

	@Test
	public void testWriteSubItemsDoesNotFailOnNullsWhenToldNotTo()
			throws Exception {

		Person megatron = new Person();

		PropertyDescriptorChain firstNameChain = new PropertyDescriptorChain("name.firstName", Person.class);
		firstNameChain.write(megatron, "Megatron", false);
		assertNull(megatron.getName());
	}

	@Test
	public void testWriteSubItems()
			throws Exception {

		Person megatron = new Person();
		megatron.setId("megatron");
		megatron.setName(new Name());

		Person starscream = new Person();
		starscream.setId("starscream");
		starscream.setName(new Name());
		

		PropertyDescriptorChain firstNameChain = new PropertyDescriptorChain("name.firstName", Person.class);
		PropertyDescriptorChain friendChain = new PropertyDescriptorChain("friend", Person.class);
		PropertyDescriptorChain friendLastNameChain = new PropertyDescriptorChain("friend.name.lastName", Person.class);

		firstNameChain.write(megatron, "Megatron", true);
		assertEquals(megatron.getName().getFirstName(), "Megatron");

		firstNameChain.write(starscream, "Starscream", true);
		assertEquals(starscream.getName().getFirstName(), "Starscream");

		friendChain.write(starscream, megatron, true);
		assertEquals(starscream.getFriend(), megatron);

		friendLastNameChain.write(starscream, "Smith", true);
		assertEquals(starscream.getFriend().getName().getLastName(), "Smith");
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
