/**
 * OPToolService.java
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
package org.apache.niolex.address.optool;

import static org.apache.niolex.address.util.PathUtil.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.address.ext.ZKOperator;

/**
 * This class encapsulates Atomic methods.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-18
 */
public class OPToolService extends ZKOperator {

    /**
     * Invoke super constructor.
     *
     * @param clusterAddress
     * @param sessionTimeout
     * @throws IOException
     */
    public OPToolService(String clusterAddress, int sessionTimeout) throws IOException {
        super(clusterAddress, sessionTimeout);
    }


    /**
     * List all services start by this prefix.
     *
     * @param prefix the service prefix
     * @return the service list
     */
    public List<String> listServiceByPrefix(String prefix) {
        List<String> childrens = getChildren(makeServicePath(root));
        if (prefix == null) {
            return childrens;
        }
        List<String> rets = new ArrayList<String>();
        for (String c : childrens) {
            if (c.startsWith(prefix))
                rets.add(c);
        }
        return rets;
    }

    /**
     * List all the services contain any server with this IP.
     *
     * @param ip the IP address to find
     * @return the service list
     */
    public List<String> listServiceByIP(String ip) {
        List<String> services = getChildren(makeServicePath(root));
        List<String> retList = new ArrayList<String>();
        for (String service : services) {
            List<String> vers = getChildren(makeService2VersionPath(root, service));
            for (String v : vers) {
                int version = 0;
                try {
                    version = Integer.parseInt(v);
                } catch (Exception e) {
                    continue;
                }
                List<String> stats = getChildren(makeService2StatePath(root, service, version));
                for (String state : stats) {
                    List<String> nodes = getChildren(makeService2NodePath(root, service, version, state));
                    for (String n : nodes) {
                        int i = n.indexOf(ip);
                        if (i != -1) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(service).append(" => ").append(version).append(' ').append(state).append(' ').append(n);
                            retList.add(sb.toString());
                        }
                    }
                }
            }
        }
        return retList;
    }

}
