/**
 * ItemInfo.java
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
package org.apache.niolex.config.bean;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-17
 */
public class ItemInfo {
    
    @NotNull
    @Size(min = 2, max = 100, message="配置组名长度必须在2~100个字符之间")
    private String groupName;
    
    @NotNull
    @Size(min = 2, max = 100, message="配置项KEY长度必须在2~100个字符之间")
    private String itemKey;
    
    @NotNull
    @Size(min = 1, max = 65535, message="配置项内容长度必须在1~65535个字符之间")
    private String itemValue;

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return the itemKey
     */
    public String getItemKey() {
        return itemKey;
    }

    /**
     * @param itemKey the itemKey to set
     */
    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    /**
     * @return the itemValue
     */
    public String getItemValue() {
        return itemValue;
    }

    /**
     * @param itemValue the itemValue to set
     */
    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }
    
}
