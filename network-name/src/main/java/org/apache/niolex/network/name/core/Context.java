/**
 * Context.java
 *
 * Copyright 2013 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.network.name.core;

import org.apache.niolex.commons.event.Event;
import org.apache.niolex.commons.event.IEventDispatcher;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.name.bean.AddressListSerializer;
import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.name.bean.AddressRecordSerializer;
import org.apache.niolex.network.name.bean.AddressRegiSerializer;
import org.apache.niolex.network.serialize.PacketTransformer;
import org.apache.niolex.network.serialize.StringSerializer;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-11-29
 */
public class Context {

    private static PacketTransformer transformer;

    public static synchronized final PacketTransformer getTransformer() {
        if (transformer == null) {
            transformer = PacketTransformer.getInstance();
            // 获取地址信息只传一个字符串，表达服务的key
            transformer.addSerializer(new StringSerializer(Config.CODE_NAME_OBTAIN));
            // 反向传输整个地址列表，表达地址
            transformer.addSerializer(new AddressListSerializer(Config.CODE_NAME_DATA));
            // 传输增量
            transformer.addSerializer(new AddressRecordSerializer(Config.CODE_NAME_DIFF));
            // 注册服务
            transformer.addSerializer(new AddressRegiSerializer(Config.CODE_NAME_PUBLISH));
        }
        return transformer;
    }

    public static synchronized final void fireEvent(IEventDispatcher dispatcher, AddressRecord rec) {
        PacketData sent = getTransformer().getPacketData(Config.CODE_NAME_DIFF, rec);
        dispatcher.fireEvent(new Event<PacketData>(rec.getAddressKey(), sent));
    }

}
