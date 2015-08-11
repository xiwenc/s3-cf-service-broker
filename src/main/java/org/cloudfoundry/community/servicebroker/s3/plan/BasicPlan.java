package org.cloudfoundry.community.servicebroker.s3.plan;

import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.amazonaws.services.identitymanagement.model.User;
import com.amazonaws.services.s3.model.Bucket;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.s3.service.Iam;
import org.cloudfoundry.community.servicebroker.s3.service.S3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicPlan {
    private final Iam iam;
    private final S3 s3;
    public static final String PLAN_ID = "s3-basic-plan";

    public BasicPlan(Iam iam, S3 s3) {
        this.iam = iam;
        this.s3 = s3;
    }

    public static Plan getPlan() {
        Plan plan = new Plan(PLAN_ID, "basic",
                "An S3 plan providing a single bucket with unlimited storage.", getPlanMetadata());
        return plan;
    }

    private static Map<String, Object> getPlanMetadata() {
        Map<String, Object> planMetadata = new HashMap<String, Object>();
        planMetadata.put("bullets", getPlanBullets());
        return planMetadata;
    }

    private static List<String> getPlanBullets() {
        return Arrays.asList("Single S3 bucket", "Unlimited storage", "Unlimited number of objects");
    }

    public ServiceInstance createServiceInstance(ServiceDefinition service, String serviceInstanceId, String planId,
                                                 String organizationGuid, String spaceGuid) {
        Bucket bucket = s3.createBucketForInstance(serviceInstanceId, service, planId, organizationGuid, spaceGuid);
        iam.createGroupForBucket(serviceInstanceId, bucket.getName());
        iam.applyGroupPolicyForBucket(serviceInstanceId, bucket.getName());
        return new ServiceInstance(serviceInstanceId, service.getId(), planId, organizationGuid, spaceGuid, null);
    }

    public ServiceInstance deleteServiceInstance(String id) {
        ServiceInstance instance = s3.findServiceInstance(id);
        // TODO we need to make these deletes idempotent so we can handle retries on error
        iam.deleteGroupPolicy(id);
        iam.deleteGroupForInstance(id);
        s3.emptyBucket(id);
        s3.deleteBucket(id);
        return instance;
    }

    public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
                                                               String serviceId, String planId, String appGuid) {
        User user = iam.createUserForBinding(bindingId);
        AccessKey accessKey = iam.createAccessKey(user);
        // TODO create password and add to credentials
        iam.addUserToGroup(user, iam.getGroupNameForInstance(serviceInstance.getId()));

        Map<String, Object> credentials = new HashMap<String, Object>();
        credentials.put("bucket", s3.getBucketNameForInstance(serviceInstance.getId()));
        credentials.put("username", user.getUserName());
        credentials.put("access_key_id", accessKey.getAccessKeyId());
        credentials.put("secret_access_key", accessKey.getSecretAccessKey());
        return new ServiceInstanceBinding(bindingId, serviceInstance.getId(), credentials, null, appGuid);
    }

    public ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
                                                               String serviceId, String planId) throws ServiceBrokerException {
        // TODO make operations idempotent so we can handle retries on error
        iam.removeUserFromGroup(bindingId, serviceInstance.getId());
        iam.deleteUserAccessKeys(bindingId);
        iam.deleteUserForBinding(bindingId);
        return new ServiceInstanceBinding(bindingId, serviceInstance.getId(), null, null, null);
    }
}
