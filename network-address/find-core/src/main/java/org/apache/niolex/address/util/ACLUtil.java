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
import java.util.Set;

import org.apache.niolex.commons.codec.Base16Util;
import org.apache.niolex.commons.codec.Base64Util;
import org.apache.niolex.commons.codec.SHAUtil;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

import com.google.common.collect.Sets;

/**
 * Zookeeper ACL related functions.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-11
 */
public class ACLUtil {

    /**
     * Get the ACL signature.
     *
     * @param name
     * @param password
     * @return the signature
     */
    public static String getSignature(String name, String password) {
        return Base64Util.byteToBase64(Base16Util.base16toByte(SHAUtil.sha1(name + ":" + password)));
    }

    /**
     * Get the Zookeeper Id.
     *
     * @param name
     * @param password
     * @return the Id
     */
    public static Id getId(String name, String password) {
        return new Id("digest", name + ":" + getSignature(name, password));
    }

    /**
     * Get the full rights of this user.
     *
     * @param name
     * @param password
     * @return the ACL list
     */
    public static List<ACL> getAllRights(String name, String password) {
        List<ACL> acls = new ArrayList<ACL>();
        ACL su = new ACL(Perms.ALL, getId(name, password));
        acls.add(su);
        return acls;
    }

    /**
     * Get the create read delete rights of this user.
     *
     * @param name
     * @param password
     * @return the ACL list
     */
    public static List<ACL> getCRDRights(String name, String password) {
        List<ACL> acls = new ArrayList<ACL>();
        ACL su = new ACL(Perms.CREATE | Perms.READ | Perms.DELETE, getId(name, password));
        acls.add(su);
        return acls;
    }

    /**
     * Get the create read delete rights of this user.
     *
     * @param name
     * @param list
     * @return the ACL list
     */
    public static List<ACL> getCRDRights(String name, List<ACL> list) {
        List<ACL> acls = new ArrayList<ACL>();
        for (ACL acl : list) {
            Id id = acl.getId();
            if (belongsTo(id, name)) {
                ACL su = new ACL(Perms.CREATE | Perms.READ | Perms.DELETE, id);
                acls.add(su);
                break;
            }
        }
        return acls;
    }

    /**
     * Get the read right of this user.
     *
     * @param name
     * @param list
     * @return the ACL list
     */
    public static List<ACL> getReadRight(String name, List<ACL> list) {
        List<ACL> acls = new ArrayList<ACL>();
        for (ACL acl : list) {
            Id id = acl.getId();
            if (belongsTo(id, name)) {
                ACL su = new ACL(Perms.READ, id);
                acls.add(su);
                break;
            }
        }
        return acls;
    }

    /**
     * Test whether this ID belongs to the specified user.
     *
     * @param id
     * @param name
     * @return true if belongs to, false otherwise
     */
    public static boolean belongsTo(Id id, String name) {
        return "digest".equals(id.getScheme()) && id.getId().startsWith(name + ":");
    }

    /**
     * Add the new ACL into the old list. If there is any duplicated IDs,
     * use the new one to replace the old one.
     *
     * @param old the old ACL list
     * @param add the new ACL list to be added
     * @return the merged ACL list
     */
    public static List<ACL> mergeACL(List<ACL> old, List<ACL> add) {
        List<ACL> tmp = new ArrayList<ACL>();
        Set<String> nameSet = Sets.newHashSet();
        for (ACL a : add) {
            if (nameSet.add(a.getId().toString())) {
                tmp.add(a);
            }
        }
        for (ACL a : old) {
            if (nameSet.add(a.getId().toString())) {
                tmp.add(a);
            }
        }
        return tmp;
    }

}
