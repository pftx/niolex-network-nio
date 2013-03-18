/**
 * QuotaInfo.java, 2012-8-13. 
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
package org.apache.niolex.address.ext;

import java.util.HashMap;
import java.util.Map;

/**
 * The quota info bean
 * 
 * @author Xie, Jiyun
 */
public class QuotaInfo {

    /**
     * Parse the quota string into quota map.
     * 
     * @param quotas the quota string
     * @return the quota map
     */
    public static Map<String, QuotaInfo> parse(String quotas) {
        Map<String, QuotaInfo> map = new HashMap<String, QuotaInfo>();
        String[] qqs = quotas.split(" *[;:] *");
        for (String s : qqs) {
            QuotaInfo i = new QuotaInfo(s);
            if (i.getClientName() != null)
                map.put(i.getClientName(), i);
        }
        return map;
    }

    // Client Name
    private String clientName;

    // The total quota for one client
    private int totalQuota;

    // The quota for one server of one client
    private int singleQuota;

    /**
     * An empty constructor, for user to create a new QuotaInfo.
     */
    public QuotaInfo() {
    }

    /**
     * Create a QuotaInfo by this string.
     * 
     * @param commaSepStr the quota string
     */
    public QuotaInfo(String commaSepStr) {
        if (commaSepStr != null) {
            String[] items = commaSepStr.split(",");
            if (items.length == 3) {
                clientName = items[0];
                totalQuota = Integer.parseInt(items[1]);
                singleQuota = Integer.parseInt(items[2]);
            }
        }
    }

    /**
     * @return the clientName
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @param clientName
     *            the clientName to set
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * @return the totalQuota
     */
    public int getTotalQuota() {
        return totalQuota;
    }

    /**
     * @param totalQuota
     *            the totalQuota to set
     */
    public void setTotalQuota(int totalQuota) {
        this.totalQuota = totalQuota;
    }

    /**
     * @return the singleQuota
     */
    public int getSingleQuota() {
        return singleQuota;
    }

    /**
     * @param singleQuota
     *            the singleQuota to set
     */
    public void setSingleQuota(int singleQuota) {
        this.singleQuota = singleQuota;
    }

    /**
     * Override super method
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{cN=").append(clientName)
                .append(", tQ=").append(totalQuota).append(", sQ=").append(singleQuota).append("}");
        return builder.toString();
    }

    /**
     * Override super method
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientName == null) ? 0 : clientName.hashCode());
        result = prime * result + singleQuota;
        result = prime * result + totalQuota;
        return result;
    }

    /**
     * Override super method
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof QuotaInfo)) return false;
        QuotaInfo other = (QuotaInfo) obj;
        if (clientName == null) return false;
        return clientName.equals(other.clientName) && singleQuota == other.singleQuota &&
                totalQuota == other.totalQuota;
    }

}
