/**
 * Environment.java
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
package org.apache.niolex.address.optool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.niolex.commons.codec.StringUtil;

/**
 * Store all the environments here.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-16
 */
public final class Environment {

    // The single instance.
    public static final Environment EVN = new Environment();

    /**
     * @return the single instance
     */
    public static final Environment getInstance() {
        return EVN;
    }

    /**
     * The login type.
     *
     * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
     * @version 1.0.0
     * @since 2013-12-16
     */
    public static enum LoginType {
        OP, SVR, CLI;
    }

    LoginType loginType = LoginType.OP;

    String host = "127.0.0.1:9181";

    int timeout = 30000;

    String userName = null;

    String password = null;

    String root = null;

    public String curpath = "/";

    boolean isSuper = false;

    boolean isInit = false;

    public boolean isLoggedIn = false;

    // Command related fields.
    public String command = null;

    public List<String> cmdArgs = null;

    /**
     * Private Constructor.
     */
    private Environment() {
    }

    /**
     * Parses a command line that may contain one or more flags before an optional command string
     *
     * @param args command line arguments
     * @return true if parsing succeeded, false otherwise.
     */
    public boolean parseOptions(String[] args) {
        List<String> argList = Arrays.asList(args);
        Iterator<String> it = argList.iterator();

        while (it.hasNext()) {
            String opt = it.next();
            try {
                if (opt.equals("-server")) {
                    this.host = it.next();
                } else if (opt.equals("-timeout")) {
                    timeout = Integer.parseInt(it.next());
                } else if (opt.equals("-auth")) {
                    String auth[] = it.next().split(":", 2);
                    userName = auth[0];
                    password = auth[1];
                } else if (opt.equals("-root")) {
                    root = it.next();
                    curpath = "/" + root;
                } else if (opt.equals("--init")) {
                    isInit = true;
                } else if (opt.equals("-login")) {
                    char c = it.next().toLowerCase().charAt(0);
                    switch (c) {
                        case 'c':
                            loginType = LoginType.CLI;
                            break;
                        case 's':
                            loginType = LoginType.SVR;
                            break;
                        case 'o':
                        default:
                            loginType = LoginType.OP;
                            break;
                    }
                }
            } catch (NoSuchElementException e) {
                System.err.println("Error: no argument found for option " + opt);
                return false;
            }

            if (!opt.startsWith("-")) {
                command = opt;
                cmdArgs = new ArrayList<String>();
                cmdArgs.add(command);
                while (it.hasNext()) {
                    cmdArgs.add(it.next());
                }
                return true;
            }
        }
        return true;
    }

    /**
     * Validate this environment.
     *
     * @return true if valid, false otherwise
     */
    public boolean validate() {
        if (root == null) {
            System.err.println("Error: root not set!");
            return false;
        }
        if (userName == null) {
            System.err.println("Error: auth not set!");
            return false;
        }
        return true;
    }

    /**
     * Breaks a string into command + arguments.
     *
     * @param cmdstring string of form "command arg1 arg2..arg-n"
     * @return true if parsing succeeded, false otherwise
     */
    public boolean parseCommand(String cmdstring) {
        String[] args = cmdstring.split("\\s+");
        if (args.length == 0) {
            return false;
        }
        command = args[0];
        cmdArgs = Arrays.asList(args);
        return true;
    }

    /**
     * Translate the relative path into absolute path.
     *
     * @param relativePath the relative path
     * @return the absolute path
     */
    public String getAbsolutePath(String relativePath) {
        if (relativePath.startsWith("./")) {
            relativePath = relativePath.substring(2);
        }
        if (StringUtil.isBlank(relativePath) || ".".equals(relativePath)) {
            return curpath;
        }
        String curpath = this.curpath;
        if ("/".equals(curpath)) {
            curpath = "";
        }
        switch (relativePath.charAt(0)) {
            case '/':
                return relativePath;
            case '.':
                break;
            default:
                return curpath + "/" + relativePath;
        }
        // OK, then there must be some ../..
        String[] breads = relativePath.split("/");
        int i = 0, j = 0;
        for (; i < breads.length; ++i) {
            if (breads[i].equals("..")) {
                ++j;
            } else {
                break;
            }
        }
        StringBuilder ret = new StringBuilder();
        String[] items = curpath.split("/");
        if (items.length > j) {
            j = items.length - j;
            // The first item in items is empty.
            for (int k = 1; k < j; ++k) {
                ret.append('/').append(items[k]);
            }
        }
        for (; i < breads.length; i++) {
            ret.append('/').append(breads[i]);
        }
        return ret.length() == 0 ? "/" : ret.toString();
    }

}