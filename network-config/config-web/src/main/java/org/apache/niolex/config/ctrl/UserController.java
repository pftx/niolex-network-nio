/**
 * UserController.java
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.niolex.config.bean.LoginInfo;
import org.apache.niolex.config.service.CenterConnector;
import org.apache.niolex.config.util.ConnectorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author <a href="mailto:jiyun.xie@renren-inc.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-12-11$
 */
@Controller
@RequestMapping(value="/user")
public class UserController {
	private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private static final String ERR = "访问配置中心出现错误,请尝试重新登录";
	
	private Validator validator;
	
    @Autowired
    public UserController(Validator validator) {
        this.validator = validator;
    }
    
    @RequestMapping(method=RequestMethod.GET)
    public String get(Model model, HttpServletRequest req) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        model.addAttribute("userName", cc.getUsername());
        return "user";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public @ResponseBody Map<String, ? extends Object> addUser(@ModelAttribute LoginInfo info,  
            HttpServletRequest req) {
    	Set<ConstraintViolation<LoginInfo>> failures = validator.validate(info);
        if (!failures.isEmpty()) {
            return Collections.singletonMap("msg", failures.iterator().next().getMessage());
        } else {
        	CenterConnector cc = ConnectorUtil.getConnector(req);
            Map<String, Object> map = new HashMap<String, Object>();
            
            if (info.getUserrole() == null) {
            	info.setUserrole("ADMIN");
            }
            
            try {
				String s = cc.addUser(info.getUsername(), info.getPassword(), info.getUserrole());
				LOG.info("Add user [{}] status - {}.", info.getUsername(), s);
				map.put("msg", s);
			} catch (Exception e) {
				LOG.error("Error occured when add user.", e);
	            map.put("msg", ERR);
			}
        	return map;
        }
    }
    
    @RequestMapping(value = "/{userName:.+}", method = RequestMethod.GET)
    public @ResponseBody Map<String, ? extends Object> queryUser(@PathVariable String userName, 
            HttpServletRequest req) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        Map<String, Object> map = new HashMap<String, Object>();
        try {
        	if (userName.length() < 2 || userName.length() > 25) {
        		map.put("msg", "用户名长度必须在2~25个字符之间。");
        		return map;
        	}
            LOG.info("User [{}] try to query user [{}].", cc.getUsername(), userName);
            String s = cc.getUser(userName);
            if (s.length() > 0) {
            	String[] s2 = s.split(",");
            	if (s2[1].equals("null")){
            		map.put("msg", "用户不存在。");            		
            	} else {
            		map.put("msg", "SUCCESS");            		
            		map.put("user", s2[0]);
            		map.put("role", s2[1]);
            	}
                return map;
            }
        } catch (Exception e) {
            LOG.error("Error occured when get config group.", e);
            map.put("msg", ERR);
        }
        return map;
    }
    
}
