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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.ItemInfo;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-14
 */
@Controller
@RequestMapping(value = "/modify")
public class ItemController {
    private static final Logger LOG = LoggerFactory.getLogger(ItemController.class);
    private static final String ERR = "访问配置中心出现错误,请尝试重新登录";
    private static final String WAR = "请不要尝试黑入系统，请通过正常途径操作";
    
    private Validator validator;
    
    @Autowired
    public ItemController(Validator validator) {
        this.validator = validator;
    }

    @RequestMapping(method = RequestMethod.GET, params={"!groupName", "!groupId", "!key"})
    public String get(Model model, HttpServletRequest req) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        model.addAttribute("userName", cc.getUsername());
        return "modify";
    }
    
    @RequestMapping(method = RequestMethod.GET, params={"groupName", "!groupId", "key"})
    public String getItem(Model model, HttpServletRequest req, @RequestParam String groupName
            , @RequestParam String key) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        model.addAttribute("userName", cc.getUsername());
        model.addAttribute("theGroupName", groupName);
        model.addAttribute("theKey", key);
        try {
            model.addAttribute("theValue", cc.getConfig(groupName).getGroupData().get(key).getValue());
        } catch (Exception e) {
            model.addAttribute("alertMessage", WAR);
        }
        return "modify";
    }
    
    @RequestMapping(method = RequestMethod.GET, params={"!groupName", "groupId", "key"})
    public String getItemFar(Model model, HttpServletRequest req, @RequestParam int groupId, @RequestParam String key) {
        CenterConnector cc = ConnectorUtil.getConnector(req);
        model.addAttribute("userName", cc.getUsername());
        String groupName = cc.findGroupName(groupId);
        model.addAttribute("theGroupName", groupName);
        model.addAttribute("theKey", key);
        try {
            model.addAttribute("theValue", cc.getConfig(groupName).getGroupData().get(key).getValue());
        } catch (Exception e) {
            model.addAttribute("alertMessage", WAR);
        }
        return "modify";
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
                map.put("list", cg.getGroupData().keySet());
            }
        } catch (Exception e) {
            LOG.error("Error occured when get config group.", e);
            map.put("msg", ERR);
        }
        return map;
    }
    
    @RequestMapping(method=RequestMethod.POST)
    public @ResponseBody Map<String, ? extends Object> updateOrCreate(@ModelAttribute ItemInfo info, HttpServletRequest req) {
        Set<ConstraintViolation<ItemInfo>> failures = validator.validate(info);
        if (!failures.isEmpty()) {
            return Collections.singletonMap("msg", failures.iterator().next().getMessage());
        } else {
            CenterConnector cc = ConnectorUtil.getConnector(req);
            try {
                String res = null;
                ConfigItem item = cc.getConfig(info.getGroupName()).getGroupData().get(info.getItemKey());
                if (item == null) {
                    res = cc.addItem(info.getGroupName(), info.getItemKey(), info.getItemValue());
                } else {
                    res = cc.updateItem(info.getGroupName(), info.getItemKey(), info.getItemValue());
                }
                if (!res.contains("success")) {
                    return Collections.singletonMap("msg", res);
                } else {
                    return Collections.singletonMap("msg", "SUCCESS");
                }
            } catch (Exception e) {
                LOG.error("Error occured when modify config item.", e);
                return Collections.singletonMap("msg", ERR);
            }
        }
    }
}
