/**
 * GroupController.java
 * 
 * Copyright 2012 RENREN, Inc.
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

import javax.servlet.http.HttpServletRequest;

import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.config.service.CenterConnector;
import org.apache.niolex.config.util.ConnectorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-12
 */
@Controller
@RequestMapping(value = "/group")
public class GroupController {
    private static final Logger LOG = LoggerFactory.getLogger(GroupController.class);
    private static final String ERR = "访问配置中心出现错误,请尝试重新登录";

    @RequestMapping(method = RequestMethod.GET)
    public String get(Model model, HttpServletRequest req) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        model.addAttribute("userName", cc.getUsername());
        return "group";
    }
    
    @RequestMapping(value = "/{groupName:.+}", method = RequestMethod.GET)
    public @ResponseBody Map<String, ? extends Object> findGroup(@PathVariable String groupName, 
            HttpServletRequest req) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            LOG.info("User [{}] try to query group [{}].", cc.getUsername(), groupName);
            String s = cc.getGroup(groupName);
            if (s.length() > 0) {
                map.put("msg", s);
                return map;
            }
            ConfigGroup cg = cc.getConfig(groupName);
            if (cg != null) {
                map.put("msg", "SUCCESS");
                map.put("list", cg.getGroupData().values());
            }
        } catch (Exception e) {
            LOG.error("Error occured when get config group.", e);
            map.put("msg", ERR);
        }
        return map;
    }

    @RequestMapping(value = "/{groupName:.+}/{key}", method = RequestMethod.GET)
    public @ResponseBody
    Map<String, ? extends Object> findItem(@PathVariable String groupName, @PathVariable String key,
            Model model, HttpServletRequest req) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        String s = null;
        try {
            s = cc.getItem(groupName, key);
        } catch (Exception e) {
            LOG.error("Error occured when get item.", e);
            s = ERR;
        }
        return Collections.singletonMap("msg", s);
    }

}
