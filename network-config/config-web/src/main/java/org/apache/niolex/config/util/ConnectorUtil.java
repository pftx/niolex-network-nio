/**
 * ConnectorUtil.java
 * 
 * Copyright 2012 The original author or authors.
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
package org.apache.niolex.config.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.niolex.config.service.CenterConnector;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-12
 */
public class ConnectorUtil {
    
    /**
     * Clear the connector in this session.
     * 
     * @param sess the http session
     */
    public static final void clearConnector(HttpSession sess) {
        if (sess != null) {
            final CenterConnector updater = (CenterConnector) sess.getAttribute("login_cli");
            if (updater != null) {
                updater.stop();
            }
        }
    }
    
    /**
     * Invalidate the current session.
     * 
     * @param req the http request
     */
    public static final void invalidateSession(HttpServletRequest req) {
        HttpSession sess = req.getSession(false);
        if (sess != null) {
            sess.invalidate();
        }
    }
    
    /**
     * Try to get the connector from session.
     * 
     * @param req the http request
     * @return the CenterConnector if found, null if not found
     */
    public static final CenterConnector getConnector(HttpServletRequest req) {
        HttpSession sess = req.getSession(false);
        if (sess != null) {
            return (CenterConnector) sess.getAttribute("login_cli");
        }
        return null;
    }

}
