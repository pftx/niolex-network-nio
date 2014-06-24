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

import org.apache.niolex.commons.event.BaseEvent;
import org.apache.niolex.commons.event.Dispatcher;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.name.bean.AddressListSerializer;
import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.name.bean.AddressRecordSerializer;
import org.apache.niolex.network.name.bean.AddressRegiSerializer;
import org.apache.niolex.network.serialize.PacketTransformer;
import org.apache.niolex.network.serialize.StringSerializer;

/**
 * Put common methods here.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-11-29
 */
public class Context {

    private static final PacketTransformer transformer;

    static {
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

    /**
     * Initialize transformer here.
     *
     * @return the transformer
     */
    public static final PacketTransformer getTransformer() {
        return transformer;
    }

    /**
     * Fire event, send this record to dispatcher.
     *
     * @param dispatcher the dispatcher used to send events
     * @param rec the record
     */
    public static final void fireEvent(Dispatcher dispatcher, AddressRecord rec) {
        PacketData sent = transformer.getPacketData(Config.CODE_NAME_DIFF, rec);
        dispatcher.fireEvent(new BaseEvent<PacketData>(rec.getAddressKey(), sent));
    }

}
