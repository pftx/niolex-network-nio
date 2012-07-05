/**
 * CodeMap.java
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
package org.apache.niolex.config.core;

/**
 * The interface to store all the packet codes.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
 */
public interface CodeMap {

	// Add.
	short GROUP_ADD = 1000;
	// Subscribe.
	short GROUP_SUB = 1001;
	// Diff.
	short GROUP_DIF = 1002;
	// Sync.
	short GROUP_SYN = 1003;
	// Data.
	short GROUP_DAT = 1004;
	// Not found.
	short GROUP_NOF = 1005;
	// No auth.
	short GROUP_NOA = 1006;

	// Auth and init subscribe.
	short AUTH_SUBS = 2000;
	// Auth failed.
	short AUTH_FAIL = 2001;
}
