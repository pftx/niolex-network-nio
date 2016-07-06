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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.commons.bean.One;
import org.apache.niolex.network.demo.proto.PersonProtos.Person;
import org.apache.niolex.network.demo.proto.PersonProtos.PhoneNumber;

/**
 * Demo implementation.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-5
 */
public class PersonServiceImpl implements PersonService {

	private Map<PhoneNumber, One<Person>> phoneMap = new ConcurrentHashMap<PhoneNumber, One<Person>>();
	private Map<Integer, One<Person>> idMap = new ConcurrentHashMap<Integer, One<Person>>();
	
	protected Person mergeSubordinates(Person source, Person target) {
	    return Person.newBuilder(target).addAllSubordinates(source.getSubordinatesList()).build();
	}
	
	protected Person addSubordinate(Person subo, Person target) {
	    return Person.newBuilder(target).addSubordinates(subo).build();
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.demo.proto.PersonService#addPerson(org.apache.niolex.network.demo.proto.PersonProtos.Person, org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneNumber)
	 */
	@Override
	public void addPerson(Person p, PhoneNumber number) {
	    One<Person> handle = phoneMap.get(number);
	    // 1. merge subordinates.
	    if (handle != null) {
	        handle.a = mergeSubordinates(handle.a, p);
	    } else {
	        handle = One.create(p);
	    }
		phoneMap.put(number, handle);
		idMap.put(p.getId(), handle);
		
		// 2. add this into parent.
		One<Person> parent = idMap.get(p.getWork().getReportTo());
		if (parent != null) {
		    parent.a = addSubordinate(p, parent.a);
		}
		System.out.println("Persion with id " + p.getId() + " added, phone " + number);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.demo.proto.PersonService#getPerson(org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneNumber)
	 */
	@Override
	public Person getPerson(PhoneNumber number) {
	    One<Person> handle = phoneMap.get(number);
		return handle != null ? handle.a : null;
	}

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.demo.proto.PersonService#updatePerson(org.apache.niolex.network.demo.proto.PersonProtos.Person)
     */
    @Override
    public Person updatePerson(Person p) {
        One<Person> handle = idMap.get(p.getId());
        if (handle != null) {
            handle.a = mergeSubordinates(handle.a, p);
        } else {
            handle = One.create(p);
        }
        idMap.put(p.getId(), handle);
        List<PhoneNumber> phoneList = p.getPhoneList();
        for (PhoneNumber num : phoneList) {
            phoneMap.put(num, handle);
        }
        return p;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.demo.proto.PersonService#clear()
     */
    @Override
    public void clear() {
        phoneMap.clear();
        idMap.clear();
    }

}
