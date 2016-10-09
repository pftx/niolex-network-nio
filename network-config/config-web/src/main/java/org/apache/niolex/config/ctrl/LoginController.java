/**
 * LoginController.java
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
package org.apache.niolex.config.ctrl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.niolex.config.bean.LoginInfo;
import org.apache.niolex.config.service.CenterConnector;
import org.apache.niolex.config.util.ConnectorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lex
 * @version 1.0.5, $Date: 2012-12-11$
 */
@Controller
@RequestMapping(value="/login")
public class LoginController {
    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);
    
    private Validator validator;
    
    private String serverAddress;
    
    @Autowired
    public LoginController(Validator validator) {
        this.validator = validator;
    }
    
    @RequestMapping(method=RequestMethod.GET, params="!signout")
    public String get(Model model) {
        return "index";
    }
    
    @RequestMapping(method=RequestMethod.GET, params="signout")
    public String signOut(HttpServletRequest req, Model model) {
        ConnectorUtil.invalidateSession(req);
        model.addAttribute("alertMessage", "您已经成功从配置中心登出，可以放心地关闭浏览器");
        return "index";
    }
    
    @RequestMapping(method=RequestMethod.POST)
    public @ResponseBody Map<String, ? extends Object> create(@ModelAttribute LoginInfo info, HttpServletRequest req) {
        Set<ConstraintViolation<LoginInfo>> failures = validator.validate(info);
        if (!failures.isEmpty()) {
            return Collections.singletonMap("msg", failures.iterator().next().getMessage());
        } else {
            CenterConnector updater = null;
            try {
                updater = new CenterConnector(serverAddress);
                String res = updater.subscribeAuthInfo(info.getUsername(), info.getPassword());
                if (!res.startsWith("SUCC")) {
                    updater.stop();
                    return Collections.singletonMap("msg", "用户名或密码错误");
                } else {
                    // Attach the updater to session.
                    HttpSession sess = req.getSession();
                    sess.setAttribute("login_cli", updater);
                    // Auth success.
                    return Collections.singletonMap("msg", "SUCCESS");
                }
            } catch (Exception e) {
                LOG.error("Error occured when connect to config center.", e);
                if (updater != null) {
                    updater.stop();
                }
                return Collections.singletonMap("msg", "无法连接到配置中心");
            }
        }
    }

    /**
     * @param serverAddress the serverAddress to set
     */
    @Required
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

}
