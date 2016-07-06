/**
 * ProtoRpcClient.java
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

import java.net.InetSocketAddress;

import org.apache.niolex.commons.test.Check;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.demo.proto.PersonProtos.Person;
import org.apache.niolex.network.demo.proto.PersonProtos.PhoneNumber;
import org.apache.niolex.network.demo.proto.PersonProtos.PhoneType;
import org.apache.niolex.network.demo.proto.PersonProtos.Work;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.conv.ProtobufConverter;

/**
 * Demo client
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-5
 */
public class DemoProtoRpcClient {
    
    /**
     * 这个是关系结构
     * 1 -- 2
     *        -- 4
     *        -- 5
     *   -- 3
     *        -- 6
     * 
     * 
     */

    static int err = 0;
    
    public static Work createWork(int reportTo, int salary, String position) {
        return Work.newBuilder().setPosition(position).setReportTo(reportTo).setSalary(salary).build();
    }
    
    public static PhoneNumber createPhoneNumber(String number, PhoneType type) {
        return PhoneNumber.newBuilder().setNumber(number).setType(type).build();
    }
    
    public static int calcReportTo(int id) {
        return id / 2;
    }

	public static void main(String[] args) throws Exception {
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
		RpcClient client = new RpcClient(c, new PacketInvoker(), new ProtobufConverter());
		client.connect();

		final PersonService ser = client.getService(PersonService.class);
		ser.clear();

		for (int i = 1; i < 10; ++i) {
		    int reportTo = calcReportTo(i);
		    String position = "";
		    switch (reportTo) {
		        case 0:
		            position = "CEO";
		            break;
		        case 1:
		            position = "DIRECTOR";
		            break;
		        case 2:
		        case 3:
		            position = "SSE/MGR";
		            break;
	            default:
	                position = "SDE";
		    }
		    Work w = createWork(reportTo, 100000 / i, position);
			PhoneNumber n = createPhoneNumber("1231223110" + i, PhoneType.WORK);
			PhoneNumber h = createPhoneNumber("1862389010" + i, PhoneType.HOME);
			Person p = Person.newBuilder().setEmail("emp000" + i + "@xxx.com").setId(i).setWork(w)
					.setName("Niolex [" + i + "]").addPhone(n).addPhone(h).build();
			ser.addPerson(p, n);
		}
		for (int i = 2; i < 5; ++i) {
			PhoneNumber n = createPhoneNumber("1231223110" + i, PhoneType.WORK);
			Person p = ser.getPerson(n);
			int reportTo = calcReportTo(i);
			Check.eq(reportTo, p.getWork().getReportTo(), "report_to");
			Check.eq(2, p.getSubordinatesCount(), "subordinates");
			System.out.println("Join ... " + p);
		}
		
		// Update
		Work w = createWork(1, 100001, "CEO");
		PhoneNumber n = createPhoneNumber("12312231188", PhoneType.WORK);
		Person p = Person.newBuilder().setEmail("ceo@xxx.com").setId(1).setWork(w)
                .setName("Steve Jobs").addPhone(n).build();
		ser.updatePerson(p);
		
		p = ser.getPerson(n);
		Check.eq(2, p.getSubordinatesCount(), "subordinates");
		System.out.println("CEO ... " + p);
		
		// Null
		n = createPhoneNumber("1231223oo88", PhoneType.WORK);
		p = ser.getPerson(n);
		Check.isTrue(p == null);
		
		Runnable r = new Runnable() {
        	final int SIZE = 2212;

			@Override
			public void run() {
				int i = SIZE;
				long in = System.currentTimeMillis();
				long maxin = 0;
				while (i-- > 0) {
					long xin = System.currentTimeMillis();
					Person p = null;
					PhoneNumber n = null;
					n = createPhoneNumber("1231223110" + 4, PhoneType.WORK);
					p = ser.getPerson(n);
					assertt(p.getName().equals("Niolex [4]"), "Out 4 => " + p);

					n = createPhoneNumber("1231223110" + 2, PhoneType.WORK);
					p = ser.getPerson(n);
					assertt(p.getName().equals("Niolex [2]"), "Out 2 => " + p);

					n = createPhoneNumber("1231223110" + 3, PhoneType.WORK);
					p = ser.getPerson(n);
					assertt(p.getName().equals("Niolex [3]"), "Out 3 => " + p);

					long xou = System.currentTimeMillis() - xin;
					if (xou > maxin) {
						maxin = xou;
					}
				}
				long t = System.currentTimeMillis() - in;
				System.out.println("rps => " + (SIZE * 3000 / t) + ", Max " + maxin + ", Avg " + (t / (SIZE * 3)));
			}};
		final int THREAD_NUM = 5;
		Thread[] ts = new Thread[THREAD_NUM];
		for (int i = 0; i < THREAD_NUM; ++i) {
			Thread t = new Thread(r);
			t.start();
			ts[i] = t;
		}
		for (int i = 0; i < THREAD_NUM; ++i) {
			ts[i].join();
			System.out.println("Join ...");
		}

		System.out.println("Done..... err count = " + err);
		client.stop();
	}

	public static void assertt(boolean b, String c) {
		if (!b) {
		    ++err;
			System.out.println(c);
		}
	}

}
