/**
 * ServiceHandlerBuilder.java
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
package org.apache.niolex.network.cli.init;

import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.cli.handler.IServiceHandler;

/**
 * Create the service handler from the complete connection Url and config bean.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-3
 */
public interface ServiceHandlerBuilder {

	/**
	 * Build the service handler instance according to the specified parameters.
	 *
	 * @param bean the config bean
	 * @param completeUrl the complete service url
	 * @return the created service handler.
	 * @throws Exception when error occurred
	 */
	public IServiceHandler build(RpcConfigBean bean, String completeUrl) throws Exception;

}
