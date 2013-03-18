/**
 * WatcherItem.java, 2012-6-21.
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

/**
 * The item to save watcher information, used when reconnect.
 *
 * @author Xie, Jiyun
 */
public class WatcherItem {

    /**
     * The path to watch.
     */
    String path;

    /**
     * The watcher.
     */
    private RecoverableWatcher watcher;

    private boolean isChildren;

    /**
     * @param path
     * @param watcher
     * @param isChildren
     */
    public WatcherItem(String path, RecoverableWatcher watcher, boolean isChildren) {
        super();
        this.path = path;
        this.watcher = watcher;
        this.isChildren = isChildren;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the watcher
     */
    public RecoverableWatcher getWat() {
        return watcher;
    }

    /**
     * @return the isChildren
     */
    public boolean isChildren() {
        return isChildren;
    }

    /**
     * Override super method
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "WatcherItem [" + path + "], isChildren=" + isChildren;
    }

}
