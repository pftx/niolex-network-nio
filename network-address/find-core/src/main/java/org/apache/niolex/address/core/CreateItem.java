/**
 * CreateItem.java, 2012-9-17.
 *
 * Copyright 2012 Niolex, Inc.
 *
 * Niolex licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.address.core;

import org.apache.zookeeper.CreateMode;

/**
 * The item to save create node information, used when reconnect.
 * 
 * @author Xie, Jiyun
 * 
 */
public class CreateItem {

    /**
     * The path to create.
     */
    private String path;

    private byte[] data;

    private CreateMode mode;

    /**
     * @param path
     * @param data
     * @param mode
     */
    public CreateItem(String path, byte[] data, CreateMode mode) {
        super();
        this.path = path;
        this.data = data;
        this.mode = mode;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the mode
     */
    public CreateMode getMode() {
        return mode;
    }

    /**
     * @param mode
     *            the mode to set
     */
    public void setMode(CreateMode mode) {
        this.mode = mode;
    }

}
