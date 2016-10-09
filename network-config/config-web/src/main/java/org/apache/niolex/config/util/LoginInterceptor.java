/**
 * LoginInterceptor.java
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.niolex.config.service.CenterConnector;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Control all the request, redirect all the not login one.
 * 
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-12
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
    private static final String KEY = "alertMessage";
    private String contextRoot = "/config-web/";
    
    @Override
    public final boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException, IOException {
        CenterConnector cc = ConnectorUtil.getConnector(request);
        if (cc == null) {
            String s = request.getRequestURI();
            if (s.equals(contextRoot) || s.contains("/login") || s.endsWith(".html")) {
                return true;
            }
            if (request.getAttribute(KEY) == null) {
                request.setAttribute(KEY, "您尚未登录或者会话已经超时，请登录后再访问该页面。");
                request.getRequestDispatcher("/login").forward(request, response);
                return false;
            } else {
                return true;
            }
        } else if (!cc.isWorking()) {
            ConnectorUtil.invalidateSession(request);
            request.setAttribute(KEY, "与配置中心的网络发生异常，请您稍后尝试重新登录。");
            request.getRequestDispatcher("/login").forward(request, response);
            return false;
        }
        return true;
    }

    /**
     * @param contextRoot the contextRoot to set
     */
    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

}
