/**
 * RpcExpose.java
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
package org.apache.niolex.address.rpc.svr;

import org.apache.niolex.address.rpc.RpcInterface;
import org.apache.niolex.address.util.VersionUtil;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.test.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RpcExpose bean, Application developer use this bean to expose their own
 * objects for remote invoke.
 *
 * Only {@link #target} is required, others are optional.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-11-30$
 */
public class RpcExpose {

    public static final Logger LOG = LoggerFactory.getLogger(RpcServer.class);
    public static final String DFT_STATE = "default";

    /**
     * The expose interface, if not set, we use the 1st interface of target.
     */
    protected Class<?> interfaze;

    /**
     * The target object to expose.
     */
    protected Object target;

    /**
     * The object state.
     */
    protected String state;

    /**
     * The weight of this server, default to 1.
     */
    protected int weight;

    /**
     * The service name of this object, default to interface name.
     */
    protected String serviceName;

    /**
     * The service type of this object, must be the same in one RpcServer.
     */
    protected String serviceType;

    /**
     * The expose version.
     */
    protected int version;

    /**
     * Create a RpcExpose with the minimum parameter.
     *
     * @param target the weight of this target
     */
    public RpcExpose(Object target) {
        super();
        this.target = target;
    }

    /**
     * Create a RpcExpose with all the proper parameters.
     *
     * @param interfaze the interface to expose
     * @param target the target to invoke
     * @param state the state of this target
     * @param weight the weight of this target
     */
    public RpcExpose(Class<?> interfaze, Object target, String state, int weight) {
        super();
        this.interfaze = interfaze;
        this.target = target;
        this.state = state;
        this.weight = weight;
    }

    /**
     * Build this RPC expose bean, check every field for validity.
     *
     * @return true if OK, false if failed to validate it
     */
    public boolean build() {
        // Fix the interface if it's not set.
        Check.notNull(target, "target must not be null.");
        if (interfaze == null) {
            interfaze = target.getClass().getInterfaces()[0];
        }
        if (StringUtil.isBlank(state)) {
            state = RpcExpose.DFT_STATE;
        }
        if (weight == 0) {
            weight = 1;
        }
        RpcInterface inter = interfaze.getAnnotation(RpcInterface.class);
        if (inter == null) {
            LOG.error("There is no annotation [RpcInterface] on {}, we can not publish this service.", interfaze);
            return false;
        }
        if (StringUtil.isBlank(serviceName)) {
            if (StringUtil.isBlank(inter.serviceName())) {
                serviceName = interfaze.getCanonicalName();
            } else {
                serviceName = inter.serviceName();
            }
        }
        if (StringUtil.isBlank(serviceType)) {
            serviceType = inter.serviceType();
        }
        if (version == 0) {
            version = VersionUtil.encodeVersion(inter.version());
        }
        return true;
    }

    /**
     * @return the interface
     */
    public Class<?> getInterfaze() {
        return interfaze;
    }

    /**
     * @param interfaze
     *            the interface to set
     */
    public void setInterfaze(Class<?> interfaze) {
        this.interfaze = interfaze;
    }

    /**
     * @return the target
     */
    public Object getTarget() {
        return target;
    }

    /**
     * @param target
     *            the target to set
     */
    public void setTarget(Object target) {
        this.target = target;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * @param weight
     *            the weight to set
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * @return the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName
     *            the serviceName to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return the serviceType
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * @param serviceType
     *            the serviceType to set
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

}
