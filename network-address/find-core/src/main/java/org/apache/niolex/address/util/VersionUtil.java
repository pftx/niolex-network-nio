/**
 * VersionUtil.java
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

import org.apache.niolex.commons.test.Check;


/**
 * Validate version, encode and decode version.
 * The common version, one may want to use this int like this:
 *       XXYYZZFFF
 *      2147483647(The Max Value)
 * So we have encode and decode methods to translate between int
 * and string format.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-17
 */
public class VersionUtil extends Check {

    public static final int MASK_MAJOR = 10000000;
    public static final int MASK_MINOR = 100000;
    public static final int MASK_PATCH = 1000;

    /**
     * Decode the int version number into string format.
     *
     * @param version the int version
     * @return the string format
     */
    public static final String decodeVersion(int version) {
        StringBuilder sb = new StringBuilder();
        int major = version / MASK_MAJOR;
        int minor = (version % MASK_MAJOR) / MASK_MINOR;
        int patch = (version % MASK_MINOR) / MASK_PATCH;
        int build = version % MASK_PATCH;
        sb.append(major).append('.').append(minor).append('.');
        sb.append(patch).append('.').append(build);
        return sb.toString();
    }

    /**
     * Encode the string version into int version number.
     *
     * @param str the string format of version
     * @return the int version number
     */
    public static final int encodeVersion(String str) {
        String[] items = str.split("\\.");
        if (items.length != 4) {
            return -1;
        }
        int major = Integer.parseInt(items[0]);
        bt(0, major, 213, "major version must between 0 and 213");
        int minor = Integer.parseInt(items[1]);
        bt(0, minor, 99, "minor version must between 0 and 99");
        int patch = Integer.parseInt(items[2]);
        bt(0, patch, 99, "patch version must between 0 and 99");
        int build = Integer.parseInt(items[3]);
        bt(0, build, 999, "build version must between 0 and 999");
        return major * MASK_MAJOR + minor * MASK_MINOR + patch * MASK_PATCH + build;
    }

    /**
     * We only support three kinds of version:
     *
     * 1 the fixed format.
     * 1+ use the current highest version greater than 1
     * 1-3 use the current highest version between 1 and 3
     *
     * @param version the string format of version
     * @return the validation result
     */
    public static final Result validateVersion(String version) {
        Result res = new Result();
        res.setValid(true);
        if (version.matches("\\d+")) {
            int ver = Integer.parseInt(version);
            res.setRange(false);
            res.setLow(ver);
        } else if (version.matches("\\d+\\+")) {
            int ver = Integer.parseInt(version.substring(0, version.length() - 1));
            res.setRange(true);
            res.setLow(ver);
            res.setHigh(Integer.MAX_VALUE);
        } else if (version.matches("\\d+\\-\\d+")) {
            res.setRange(true);
            String[] lh = version.split("-");
            res.setLow(Integer.parseInt(lh[0]));
            res.setHigh(Integer.parseInt(lh[1]));
        } else {
            res.setValid(false);
        }
        return res;
    }

    /**
     * The version validation result.
     *
     * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
     * @version 1.0.0
     * @since 2013-12-11
     */
    public static class Result {
        private boolean isValid;
        private boolean isRange;
        private int low;
        private int high;

        /**
         * @return the isValid
         */
        public boolean isValid() {
            return isValid;
        }

        /**
         * @param isValid the isValid to set
         */
        public void setValid(boolean isValid) {
            this.isValid = isValid;
        }

        /**
         * @return the isRange
         */
        public boolean isRange() {
            return isRange;
        }

        /**
         * @param isRange the isRange to set
         */
        public void setRange(boolean isRange) {
            this.isRange = isRange;
        }

        /**
         * @return the low
         */
        public int getLow() {
            return low;
        }

        /**
         * @param low the low to set
         */
        public void setLow(int low) {
            this.low = low;
        }

        /**
         * @return the high
         */
        public int getHigh() {
            return high;
        }

        /**
         * @param high the high to set
         */
        public void setHigh(int high) {
            this.high = high;
        }

        @Override
        public String toString() {
            return "{V?" + isValid + ", R?" + isRange + ", [" + low + ", " + high + ")}";
        }

    }
}
