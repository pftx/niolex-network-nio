/**
 * ConverterCenter.java
 *
 * Copyright 2012 Niolex, Inc.
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
package org.apache.niolex.address.rpc;

import java.util.HashMap;
import java.util.Map;

import org.apache.niolex.network.rpc.IConverter;
import org.apache.niolex.network.rpc.conv.JsonConverter;
import org.apache.niolex.network.rpc.conv.ProtobufConverter;
import org.apache.niolex.network.rpc.conv.ProtoStuffConverter;

/**
 * Manage all the converters available. Client can use {@link #addConverter(String, IConverter)}
 * to add customized converters for special purpose.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-11-30$
 */
public class ConverterCenter {

    /**
     * The internal hash map of converters.
     */
    private static Map<String, IConverter> builderMap = new HashMap<String, IConverter>();

    /**
     * We put all the network-rpc default converters into the map.
     */
    static {
        builderMap.put("network/json", new JsonConverter());
        builderMap.put("network/proto", new ProtobufConverter());
        builderMap.put("network/stuff", new ProtoStuffConverter());
    }

    /**
     * Add a custom converter into converter center.
     *
     * @param serviceType the service type i.e. network/amqp
     * @param iconv the converter
     */
    public static void addConverter(String serviceType, IConverter iconv) {
        builderMap.put(serviceType, iconv);
    }

    /**
     * Get the converter associated with this service type.
     *
     * @param serviceType the service type
     * @return the related converter or null if not found
     */
    public static IConverter getConverter(String serviceType) {
        return builderMap.get(serviceType);
    }

}
