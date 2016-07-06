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
     * @param name the user name
     * @param password the password
     * @return the signature
     */
    public static String getSignature(String name, String password) {
        return Base64Util.byteToBase64(Base16Util.base16toByte(SHAUtil.sha1(name + ":" + password)));
    }

    /**
     * Get the Zookeeper Id.
     *
     * @param name the user name
     * @param password the password
     * @return the Id
     */
    public static Id getId(String name, String password) {
        return new Id("digest", name + ":" + getSignature(name, password));
    }

    /**
     * Get the only Id from the specified ACL list.
     * 
     * @param acl the ACL list
     * @return the only Id if found
     * @throws IllegalArgumentException if the specified ACL list is invalid
     */
    public static Id getId(List<ACL> acl) {
        if (acl.size() != 1) {
            throw new IllegalArgumentException("ACL list must have exactly one element.");
        }
        return acl.get(0).getId();
    }

    /**
     * Get the user's name from this Id.
     *
     * @param id the zookeeper id
     * @return the user's name, or null if not found
     */
    public static String getUserName(Id id) {
        if ("digest".equals(id.getScheme())) {
            String sig = id.getId();
            return sig.substring(0, sig.indexOf(':'));
        }
        return null;
    }

    /**
     * Get the full rights of this user.
     *
     * @param name the user's name
     * @param password the user's password
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
     * @param name the user's name
     * @param password the user's password
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
     * @param name the user's name
     * @param list the original ACL list
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
     * @param name the user's name
     * @param list the original ACL list
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
     * @param id the zookeeper Id
     * @param name the user's name
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
        Set<Id> nameSet = Sets.newHashSet();
        for (ACL a : add) {
            if (nameSet.add(a.getId())) {
                tmp.add(a);
            }
        }
        for (ACL a : old) {
            if (nameSet.add(a.getId())) {
                tmp.add(a);
            }
        }
        return tmp;
    }

    /**
     * Remove all the ACLs belongs to the specified Id.
     *
     * @param old the old ACL list
     * @param id the Id to be removed
     * @return the result
     */
    public static List<ACL> removeId(List<ACL> old, Id id) {
        List<ACL> tmp = new ArrayList<ACL>();
        for (ACL a : old) {
            if (!id.equals(a.getId())) {
                tmp.add(a);
            }
        }
        return tmp;
    }


    /**
     * Generate formatted string from the specified ACL list.
     *
     * @param list the ACL list
     * @return the formatted string
     */
    public static String formatACL(List<ACL> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("total ").append(list.size()).append('\n');
        for (ACL acl : list) {
            formatACL(sb, acl);
        }
        return sb.toString();
    }

    /**
     * Generate formatted string from the specified ACL, and append
     * the output to the string builder.
     *
     * @param sb the string builder
     * @param acl the ACL
     */
    public static void formatACL(StringBuilder sb, ACL acl) {
        sb.append("\t");
        int p = acl.getPerms();
        if ((p & Perms.ADMIN) != 0) {
            sb.append('A');
        } else {
            sb.append('-');
        }
        if ((p & Perms.CREATE) != 0) {
            sb.append('C');
        } else {
            sb.append('-');
        }
        if ((p & Perms.DELETE) != 0) {
            sb.append('D');
        } else {
            sb.append('-');
        }
        if ((p & Perms.READ) != 0) {
            sb.append('R');
        } else {
            sb.append('-');
        }
        if ((p & Perms.WRITE) != 0) {
            sb.append('W');
        } else {
            sb.append('-');
        }
        String name = acl.getId().getId();
        int i = name.indexOf(':');
        if (i != -1)
            name = name.substring(0, i);
        sb.append(' ').append(name).append('\n');
    }

}
