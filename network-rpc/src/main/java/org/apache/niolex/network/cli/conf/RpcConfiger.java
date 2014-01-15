/**
 * RpcConfiger.java
 *
 * Copyright 2011 Niolex, Inc.
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
package org.apache.niolex.network.cli.conf;

import java.io.IOException;
import java.io.InputStream;

/**
 * RpcConfiger, create RpcConfigBean here.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-5-27
 */
public class RpcConfiger extends BaseConfiger<RpcConfigBean> {

    /**
     * Init the Configer with a property file, relative to the class path of this class.
     *
     * @param fileName property file path name(which must in classpath)
     * @throws IOException when error occurred
     */
	public RpcConfiger(String fileName) throws IOException {
		super(fileName);
	}

	/**
     * Reads a property list (key and element pairs) from the input byte stream.
     * The input stream is in a simple line-oriented format and is assumed to use the ISO 8859-1 character encoding;
     * that is each byte is one Latin1 character. Characters not in Latin1, and certain special characters,
     * are represented in keys and elements using Unicode escapes.
     *
     * The specified stream will be closed after this method returns.
     *
     * @param inStream the stream contains property list
     * @param instanceMark the string mark this configer
     * @throws IOException when error occurred
     */
	public RpcConfiger(InputStream inStream, String instanceMark) throws IOException {
		super(inStream, instanceMark);
	}

	/**
	 * Create RpcConfigBean here.
	 *
	 * Override super method
	 * @see org.apache.niolex.network.cli.conf.BaseConfiger#newConfigBean(java.lang.String)
	 */
	@Override
	protected RpcConfigBean newConfigBean(String groupName) {
		return new RpcConfigBean(groupName);
	}

	/**
	 * Get the default config if there is any.
	 *
	 * @return the default config bean if group not exist, null otherwise
	 */
	@Override
	public RpcConfigBean getConfig() {
		return (RpcConfigBean)super.getConfig(BaseConfiger.DEFAULT);
	}

}
