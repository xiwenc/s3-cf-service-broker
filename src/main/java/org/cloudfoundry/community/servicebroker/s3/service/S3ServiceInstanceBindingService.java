/*
 * Copyright 2014 the original author or authors.
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
package org.cloudfoundry.community.servicebroker.s3.service;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.s3.plan.BasicPlan;
import org.cloudfoundry.community.servicebroker.s3.plan.SharedPlan;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author David Ehringer
 */
@Service
public class S3ServiceInstanceBindingService implements ServiceInstanceBindingService {

    private final S3 s3;
    private final Iam iam;
    private final SharedPlan sharedPlan;
    private final BasicPlan basicPlan;

    @Autowired
    public S3ServiceInstanceBindingService(S3 s3, Iam iam, BasicPlan basicPlan, SharedPlan sharedPlan) {
        this.s3 = s3;
        this.iam = iam;
        this.basicPlan = basicPlan;
        this.sharedPlan = sharedPlan;
    }

    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
            String serviceId, String planId, String appGuid) throws ServiceInstanceBindingExistsException,
            ServiceBrokerException {

        if (planId.equals(BasicPlan.PLAN_ID)) {
            return basicPlan.createServiceInstanceBinding(bindingId, serviceInstance, serviceId, planId, appGuid);
        } else if (planId.equals(SharedPlan.PLAN_ID)) {
            return sharedPlan.createServiceInstanceBinding(bindingId, serviceInstance, serviceId, planId, appGuid);
        }
        return null;
    }

    @Override
    public ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
            String serviceId, String planId) throws ServiceBrokerException {

        if (planId.equals(BasicPlan.PLAN_ID)) {
            return basicPlan.deleteServiceInstanceBinding(bindingId, serviceInstance, serviceId, planId);
        } else if (planId.equals(SharedPlan.PLAN_ID)) {
            return sharedPlan.deleteServiceInstanceBinding(bindingId, serviceInstance, serviceId, planId);
        }
        return null;
    }

    @Override
    public ServiceInstanceBinding getServiceInstanceBinding(String id) {
        throw new IllegalStateException("Not implemented");
    }

}
