/**
 * ConnStatus.java
 *
 * Copyright 2013 The original author or authors.
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
package org.apache.niolex.network;

/**
 * The connection status in this framework.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-1
 */
public enum ConnStatus {

    INNITIAL("The connection is initializing."),
    CONNECTING("Trying to connect to server."),
    CONNECTED("Connection is ready for use."),
    CLOSED("Connection is closed.");

    /**
     * the detailed explanation for the status.
     */
    private final String explanation;

    /**
     * Create a connection status type with detailed explanation.
     *
     * @param explanation the explanation of this status
     */
    private ConnStatus(String explanation) {
        this.explanation = explanation;
    }

    /**
     * Get the detailed explanation for the status.
     *
     * @return the detailed explanation
     */
    public String getExplanation() {
        return explanation;
    }

}
