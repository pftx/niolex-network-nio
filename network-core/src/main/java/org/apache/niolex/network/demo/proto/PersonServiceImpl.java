/**
 * PersonServiceImpl.java
 *
 * Copyright 2012 Niolex, Inc.
 *
 * Niolex licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.niolex.network.demo.proto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.network.demo.proto.PersonProtos.Person;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneNumber;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public class PersonServiceImpl implements PersonService {

	private Map<PhoneNumber, Person> map = new ConcurrentHashMap<PhoneNumber, Person>();

	/**
	 * Override super method
	 * @see org.apache.niolex.network.demo.proto.PersonService#addPerson(org.apache.niolex.network.demo.proto.PersonProtos.Person)
	 */
	@Override
	public void addPerson(Person p, PhoneNumber number) {
		map.put(number, p);
		System.out.println("Persion with id " + p.getId() + " added, phone " + number);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.demo.proto.PersonService#getPerson(int)
	 */
	@Override
	public Person getPerson(PhoneNumber number) {
		return map.get(number);
	}

}
