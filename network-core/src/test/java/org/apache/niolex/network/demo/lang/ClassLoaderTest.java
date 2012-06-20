/**
 * ClassLoaderTest.java
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
package org.apache.niolex.network.demo.lang;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.commons.reflect.ProxyUtil;
import org.apache.niolex.commons.reflect.ProxyUtil.ProxyHandler;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-18
 */
public class ClassLoaderTest {

	public interface KTestI {
		public Integer getB();
	}

	public class KTestBean implements KTestI {
		Integer b;

		public Integer getB() {
			return b;
		}

		public void setB(Integer b) {
			this.b = b;
		}

	}

	@Test
	public void test() throws Throwable {
		// Class for name will execute.
		// Class.forName("org.apache.niolex.network.demo.lang.VolatileTest");
		// Load class will not execute static block.
		ClassLoaderTest.class.getClassLoader().loadClass("org.apache.niolex.network.demo.lang.VolatileTest");

		Method m = MethodUtil.getMethod(KTestBean.class, "getB", new Class<?>[0]);
		System.out.println(m);
		KTestBean test = new KTestBean();
		test.setB(123);

		final int SIZE = 2 << 20;
		final Integer TEST = 123;
		Object param[] = new Object[0];

		long in = System.currentTimeMillis();
		for (int i = 0; i < SIZE; ++i) {
			Integer k = (Integer) m.invoke(test, param);
			assertEquals(k, TEST);
		}

		long t = System.currentTimeMillis() - in;
		System.out.println("Method Time: " + t);

		Field f = FieldUtil.getField(KTestBean.class, "b");
		in = System.currentTimeMillis();
		for (int i = 0; i < SIZE; ++i) {
			Integer k = (Integer) f.get(test);
			assertEquals(k, TEST);
		}
		t = System.currentTimeMillis() - in;
		System.out.println("Field Time: " + t);

		in = System.currentTimeMillis();
		for (int i = 0; i < SIZE; ++i) {
			Integer k = test.getB();
			assertEquals(k, TEST);
		}
		t = System.currentTimeMillis() - in;
		System.out.println("Direct m Time: " + t);

		in = System.currentTimeMillis();
		for (int i = 0; i < SIZE; ++i) {
			Integer k = test.b;
			assertEquals(k, TEST);
		}
		t = System.currentTimeMillis() - in;
		System.out.println("Direct f Time: " + t);
	}

	class A extends B {
		public int a = 100;

		public A() {
			super();
			System.out.println(a);
			a = 200;
		}

	}

	class B {
		public B() {
			System.out.println(((A) this).a);
		}
	}

	@Test
	public void tem() {
		System.out.println(new A().a);
		KTestBean test = new KTestBean();
		test.setB(123);

		final int SIZE = 2 << 20;
		final Integer TEST = 123;

		ProxyHandler h = new ProxyHandler() {
			@Override
			public void invokeBefore(Object proxy, Method method, Object[] args) {
			}

			@Override
			public void invokeAfter(Object proxy, Method method, Object[] args, Object ret) {
			}
		};
		KTestI proxy = ProxyUtil.newProxyInstance(test, h);

		long in = System.currentTimeMillis();
		for (int i = 0; i < SIZE; ++i) {
			Integer k = proxy.getB();
			assertEquals(k, TEST);
		}

		long t = System.currentTimeMillis() - in;
		System.out.println("Method Time: " + t);

		in = System.currentTimeMillis();
		for (int i = 0; i < SIZE; ++i) {
			Integer k = test.getB();
			assertEquals(k, TEST);
		}
		t = System.currentTimeMillis() - in;
		System.out.println("Grig M Time: " + t);
	}
}
