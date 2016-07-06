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

import org.apache.niolex.address.util.PathUtil.Path.Level;


/**
 * The whole path of service is:
 * /&lt;root&gt;/services/&lt;service&gt;/versions/&lt;version&gt;/&lt;state&gt;/&lt;node&gt;
 * The whole path of meta data is:
 * /&lt;root&gt;/services/&lt;service&gt;/clients/&lt;version&gt;/&lt;client-name&gt;
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
     * @param root the zookeeper root
     * @return the path
     */
    public static String makeOpPath(String root) {
        return new StringBuilder().append(root).append("/").append(OP_ROOT).toString();
    }

    /**
     * Make the server path.
     *
     * @param root the zookeeper root
     * @return the path
     */
    public static String makeServerPath(String root) {
        return new StringBuilder().append(root).append("/").append(SVR_ROOT).toString();
    }

    /**
     * Make the client path.
     *
     * @param root the zookeeper root
     * @return the path
     */
    public static String makeClientPath(String root) {
        return new StringBuilder().append(root).append("/").append(CLI_ROOT).toString();
    }

    /**
     * Make the service path.
     *
     * @param root the zookeeper root
     * @return the path
     */
    public static String makeServicePath(String root) {
        return new StringBuilder().append(root).append("/").append(SERVICES).toString();
    }

    /**
     * Make the operator path.
     *
     * @param root the zookeeper root
     * @param opName the operator name
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
     * @param root the zookeeper root
     * @param svrName the server name
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
     * @param root the zookeeper root
     * @param cliName the client name
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
     * @param root the zookeeper root
     * @param service the service name
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
     * @param root the zookeeper root
     * @param service the service name
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
     * @param root the zookeeper root
     * @param service the service name
     * @param version the service version
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
     * @param root the zookeeper root
     * @param service the service name
     * @param version the service version
     * @param state the state name
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
     * @param root the zookeeper root
     * @param service the service name
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
     * @param root the zookeeper root
     * @param service the service name
     * @param version the service version
     * @return the path
     */
    public static String makeMeta2VersionPath(String root, String service, int version) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(SERVICES).append("/").append(service).append("/").append(CLI_ROOT);
        path.append("/").append(version);
        return path.toString();
    }

    /**
     * Make the meta data to client node path.
     *
     * @param root the zookeeper root
     * @param service the service name
     * @param version the service version
     * @param clientName the client name
     * @return the path
     */
    public static String makeMeta2NodePath(String root, String service, int version, String clientName) {
        StringBuilder path = new StringBuilder();
        path.append(root).append("/").append(SERVICES).append("/").append(service).append("/").append(CLI_ROOT);
        path.append("/").append(version).append("/").append(clientName);
        return path.toString();
    }

    /**
     * Decode the current path.
     *
     * @param root the root of find storage
     * @param currentPath the current path
     * @return the decoded path bean
     */
    public static Path decodePath(String root, String currentPath) {
        if (root.startsWith("/")) {
            root = root.substring(1);
        }
        Path p = new Path();
        String[] items = currentPath.split("/");
        if (items.length < 2 || !root.equals(items[1])) {
            p.level = Level.OTHER;
            return p;
        }
        p.root = items[1];
        if (items.length == 2) {
            p.level = Level.ROOT;
            return p;
        }
        if (!SERVICES.equals(items[2])) {
            p.level = Level.RO_OTHER;
            return p;
        }
        if (items.length == 3) {
            p.level = Level.RO_SER;
            return p;
        }
        p.service = items[3];
        if (items.length == 4) {
            p.level = Level.SERVICE;
            return p;
        }
        if (VERSIONS.equals(items[4])) {
            if (items.length == 5) {
                p.level = Level.SER_VER;
                return p;
            }
            try {
                p.version = Integer.parseInt(items[5]);
            } catch (Exception e) {
                p.version = -1;
                p.level = Level.SER_VER;
                return p;
            }
            if (items.length == 6) {
                p.level = Level.SVERSION;
                return p;
            }
            p.state = items[6];
            if (items.length == 7) {
                p.level = Level.STATE;
                return p;
            }
            p.node = items[7];
            p.level = Level.NODE;
            return p;
        } else if (CLI_ROOT.equals(items[4])) {
            if (items.length == 5) {
                p.level = Level.SER_CLI;
                return p;
            }
            try {
                p.version = Integer.parseInt(items[5]);
            } catch (Exception e) {
                p.version = -1;
                p.level = Level.SER_CLI;
                return p;
            }
            if (items.length == 6) {
                p.level = Level.CVERSION;
                return p;
            }
            p.client = items[6];
            p.level = Level.CLIENT;
            return p;
        } else {
            p.level = Level.SER_OTHER;
            return p;
        }
    }

    /**
     * The decoded path.
     *
     * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
     * @version 1.0.0
     * @since 2013-12-17
     */
    public static class Path {

        /**
         * The path level.
         *
         * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
         * @version 1.0.0
         * @since 2013-12-17
         */
        public static enum Level {
            OTHER, ROOT, RO_SER, RO_OTHER, SERVICE,
            SER_VER, SER_CLI, SER_OTHER,
            SVERSION, CVERSION, CLIENT, STATE, NODE;
        }

        private Level level;
        private String root;
        private String service;
        private int version;
        private String state;
        private String node;
        private String client;

        /**
         * @return the level
         */
        public Level getLevel() {
            return level;
        }
        /**
         * @return the root
         */
        public String getRoot() {
            return root;
        }
        /**
         * @return the service
         */
        public String getService() {
            return service;
        }
        /**
         * @return the version
         */
        public int getVersion() {
            return version;
        }
        /**
         * @return the state
         */
        public String getState() {
            return state;
        }
        /**
         * @return the node
         */
        public String getNode() {
            return node;
        }
        /**
         * @return the client
         */
        public String getClient() {
            return client;
        }

    }

}
