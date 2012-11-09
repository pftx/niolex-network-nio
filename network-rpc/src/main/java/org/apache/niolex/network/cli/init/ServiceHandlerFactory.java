/**
 * ServiceHandlerFactory.java
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

import java.util.HashMap;
import java.util.Map;

/**
 * The factory method of ServiceHandlerBuilder.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-19
 */
public class ServiceHandlerFactory {

	/**
	 * The internal hash map of builders.
	 */
	private static Map<String, ServiceHandlerBuilder> builderMap = new HashMap<String, ServiceHandlerBuilder>();

	/**
	 * Register builder.
	 *
	 * @param serviceType
	 * @param builder
	 */
	public static void registerBuilder(String serviceType, ServiceHandlerBuilder builder) {
		builderMap.put(serviceType, builder);
	}

	/**
	 * Get builder for this service type.
	 *
	 * @param serviceType
	 * @return
	 */
	public static ServiceHandlerBuilder getBuilder(String serviceType) {
		ServiceHandlerBuilder builder = builderMap.get(serviceType);
		if (builder == null) {
			throw new IllegalArgumentException("Builder for " + serviceType + " not found.");
		}
		return builder;
	}
}
