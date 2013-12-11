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


/**
 * The quota info bean
 *
 * @author Xie, Jiyun
 */
public class QuotaInfo {

    /**
     * Parse the quota string into quota bean.
     *
     * @param quotaStr the quota string
     * @return the quota info bean
     */
    public static final QuotaInfo parse(String quotaStr) {
        String[] items = quotaStr.split("\\s*[,;:]\\s*");
        QuotaInfo i = new QuotaInfo();
        if (items.length >= 2) {
            i.secondQuota = Integer.parseInt(items[0]);
            i.minuteQuota = Integer.parseInt(items[1]);
        }
        return i;
    }

    // The total quota for one client
    private int secondQuota;

    // The quota for one server of one client
    private int minuteQuota;

    /**
     * An empty constructor, for user to create a new QuotaInfo.
     */
    public QuotaInfo() {
    }

    /**
     * @return the second quota
     */
    public int getSecondQuota() {
        return secondQuota;
    }

    /**
     * Set the second quota
     *
     * @param secondQuota
     */
    public void setSecondQuota(int secondQuota) {
        this.secondQuota = secondQuota;
    }

    /**
     * @return get the minute quota
     */
    public int getMinuteQuota() {
        return minuteQuota;
    }

    /**
     * Set the minute quota
     *
     * @param minuteQuota
     */
    public void setMinuteQuota(int minuteQuota) {
        this.minuteQuota = minuteQuota;
    }

    /**
     * Override super method
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{secQ=").append(secondQuota).append(", minQ=").append(minuteQuota).append("}");
        return builder.toString();
    }

    /**
     * Override super method
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 31 * secondQuota + minuteQuota;
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
        return secondQuota == other.secondQuota && minuteQuota == other.minuteQuota;
    }

}
