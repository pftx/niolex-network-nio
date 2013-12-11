/**
 * ACLUtil.java
 *
 * Copyright 2013 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.address.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.codec.Base16Util;
import org.apache.niolex.commons.codec.Base64Util;
import org.apache.niolex.commons.codec.SHAUtil;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-11
 */
public class ACLUtil {

    public static String getSignature(String name, String password) {
        return Base64Util.byteToBase64(Base16Util.base16toByte(SHAUtil.sha1(name + ":" + password)));
    }

    public static Id getId(String name, String password) {
        return new Id("digest", name + ":" + getSignature(name, password));
    }

    public static List<ACL> getAllRights(String name, String password) {
        List<ACL> acls = new ArrayList<ACL>();
        ACL su = new ACL(Perms.ALL, getId(name, password));
        acls.add(su);
        return acls;
    }

    public static List<ACL> getCRDRights(String name, String password) {
        List<ACL> acls = new ArrayList<ACL>();
        ACL su = new ACL(Perms.CREATE | Perms.READ | Perms.DELETE, getId(name, password));
        acls.add(su);
        return acls;
    }

    public static List<ACL> mergeACL(List<ACL> old, List<ACL> add) {
        List<ACL> tmp = new ArrayList<ACL>();
        tmp.addAll(old);
        tmp.addAll(add);
        return tmp;
    }

}
