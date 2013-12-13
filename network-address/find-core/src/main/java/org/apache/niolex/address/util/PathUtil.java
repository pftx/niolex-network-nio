/**
 * PathUtil.java
 *
 * Copyright 2013 The original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.address.util;

/**
 * The whole path of service is:
 * /<root>/services/<service>/versions/<version>/<state>/<node>
 * The whole path of meta data is:
 * /<root>/services/<service>/clients/<version>/<client-name>
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-15
 */
public abstract class PathUtil {
    public static final String OP_ROOT = "operators";
    public static final String SVR_ROOT = "servers";
    public static final String CLI_ROOT = "clients";
    public static final String SERVICES = "services";
    public static final String VERSIONS = "versions";

    /**
     * Make the operator path.
     *
     * @param root
     * @return the path
     */
    public static String makeOpPath(String root) {
        return new StringBuilder().append(root).append("/").append(OP_ROOT).toString();
    }

    /**
     * Make the server path.
     *
     * @param root
     * @return the path
     */
    public static String makeServerPath(String root) {
        return new StringBuilder().append(root).append("/").append(SVR_ROOT).toString();
    }

    /**
     * Make the client path.
     *
     * @param root
     * @return the path
     */
    public static String makeClientPath(String root) {
        return new StringBuilder().append(root).append("/").append(CLI_ROOT).toString();
    }

    /**
     * Make the service path.
     *
     * @param root
     * @return the path
     */
    public static String makeServicePath(String root) {
        return new StringBuilder().append(root).append("/").append(SERVICES).toString();
    }

    /**
     * Make the operator path.
     *
     * @param root
     * @param opName
     * @return the path
     */
    public static String makeOpPath(String root, String opName) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(OP_ROOT).append("/").append(opName);
        return path.toString();
    }

    /**
     * Make the server path.
     *
     * @param root
     * @param svrName
     * @return the path
     */
    public static String makeServerPath(String root, String svrName) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(SVR_ROOT).append("/").append(svrName);
        return path.toString();
    }

    /**
     * Make the client path.
     *
     * @param root
     * @param cliName
     * @return the path
     */
    public static String makeClientPath(String root, String cliName) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(CLI_ROOT).append("/").append(cliName);
        return path.toString();
    }

    /**
     * Make the service path.
     *
     * @param root
     * @param service
     * @return the path
     */
    public static String makeServicePath(String root, String service) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(SERVICES).append("/").append(service);
        return path.toString();
    }

    /**
     * Make the service to version path.
     *
     * @param root
     * @param service
     * @return the path
     */
    public static String makeService2VersionPath(String root, String service) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(SERVICES).append("/").append(service).append("/").append(VERSIONS);
        return path.toString();
    }

    /**
     * Make the service to state path.
     *
     * @param root
     * @param service
     * @param version
     * @return the path
     */
    public static String makeService2StatePath(String root, String service, int version) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(SERVICES).append("/").append(service).append("/").append(VERSIONS);
        path.append("/").append(version);
        return path.toString();
    }

    /**
     * Make the service to state path.
     *
     * @param root
     * @param service
     * @param version
     * @param state
     * @return the path
     */
    public static String makeService2NodePath(String root, String service, int version, String state) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(SERVICES).append("/").append(service).append("/").append(VERSIONS);
        path.append("/").append(version).append("/").append(state);
        return path.toString();
    }

    /**
     * Make the meta data to client path.
     *
     * @param root
     * @param service
     * @return the path
     */
    public static String makeMeta2ClientPath(String root, String service) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(SERVICES).append("/").append(service).append("/").append(CLI_ROOT);
        return path.toString();
    }


    /**
     * Make the meta data to version path.
     *
     * @param root
     * @param service
     * @param version
     * @return the path
     */
    public static String makeMeta2VersionPath(String root, String service, int version) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(SERVICES).append("/").append(service).append("/").append(CLI_ROOT);
        path.append("/").append(version);
        return path.toString();
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
