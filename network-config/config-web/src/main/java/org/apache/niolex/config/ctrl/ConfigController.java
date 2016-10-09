/**
 * ItemController.java
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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.niolex.config.service.CenterConnector;
import org.apache.niolex.config.util.ConnectorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-14
 */
@Controller
@RequestMapping(value = "/config")
public class ConfigController {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigController.class);
    private static final String ERR = "访问配置中心出现错误,请尝试重新登录";

    @RequestMapping(method = RequestMethod.GET, params={"!groupName"})
    public String get(Model model, HttpServletRequest req) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        model.addAttribute("userName", cc.getUsername());
        return "config";
    }
    
    @RequestMapping(method = RequestMethod.GET, params={"groupName"})
    public @ResponseBody Map<String, ? extends Object> getGroup(HttpServletRequest req, @RequestParam String groupName) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            LOG.info("User [{}] try to query group [{}].", cc.getUsername(), groupName);
            String s = cc.getGroup(groupName);
            if (s.contains("Group not found")) {
                map.put("msg", "SUCCESS");
            } else {
                if (s.length() == 0) {
                    s = "该配置组已经存在，请您与管理员确认。";
                }
                map.put("msg", s);
            }
        } catch (Exception e) {
            LOG.error("Error occured when get config group.", e);
            map.put("msg", ERR);
        }
        return map;
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Map<String, ? extends Object> findGroup(@RequestParam String groupName, 
            HttpServletRequest req) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            LOG.info("User [{}] try to add group [{}].", cc.getUsername(), groupName);
            String s = cc.addGroup(groupName);
            if (!s.contains("success")) {
                map.put("msg", s);
                return map;
            } else {
                map.put("msg", "SUCCESS");
            }
        } catch (Exception e) {
            LOG.error("Error occured when add config group.", e);
            map.put("msg", ERR);
        }
        return map;
    }
}
