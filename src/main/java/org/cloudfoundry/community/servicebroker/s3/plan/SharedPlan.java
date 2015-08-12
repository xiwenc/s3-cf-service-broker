package org.cloudfoundry.community.servicebroker.s3.plan;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.s3.config.BrokerConfiguration;
import org.cloudfoundry.community.servicebroker.s3.service.Iam;
import org.cloudfoundry.community.servicebroker.s3.service.S3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SharedPlan {
    private final BrokerConfiguration brokerConfiguration;

    private final Iam iam;
    private final S3 s3;
    private AmazonS3 s3client;
    public static final String PLAN_ID = "s3-shared-plan";
    public static final String CONFIG_DIR = "config";

    @Autowired
    public SharedPlan(Iam iam, S3 s3, BrokerConfiguration brokerConfiguration) {
        this.iam = iam;
        this.s3 = s3;
        this.brokerConfiguration = brokerConfiguration;
        s3client = brokerConfiguration.amazonS3();
    }

    public static Plan getPlan() {
        Plan plan = new Plan(PLAN_ID, "shared",
                "An S3 plan providing a shared bucket with unlimited storage.", getPlanMetadata());
        return plan;
    }

    private void ensureBucket() {
        if (!s3client.doesBucketExist(brokerConfiguration.getSharedBucket())) {
            s3client.createBucket(brokerConfiguration.getSharedBucket(), brokerConfiguration.getRegion());
            //s3client.setBucketPolicy();
        }
    }

    private static Map<String, Object> getPlanMetadata() {
        Map<String, Object> planMetadata = new HashMap<String, Object>();
        planMetadata.put("bullets", getPlanBullets());
        return planMetadata;
    }

    private static List<String> getPlanBullets() {
        return Arrays.asList("Shared S3 bucket", "Unlimited storage", "Unlimited number of objects");
    }

    private String getInstanceConfigPath(String instanceId) {
        return String.format("%s/%s", CONFIG_DIR, instanceId);
    }

    public ServiceInstance createServiceInstance(ServiceDefinition service, String serviceInstanceId, String planId,
                                                 String organizationGuid, String spaceGuid) {
        String instanceConfigPath = getInstanceConfigPath(serviceInstanceId);
        s3client.putObject(new PutObjectRequest(brokerConfiguration.getSharedBucket(), instanceConfigPath, new File("/dev/null")));
        return new ServiceInstance(serviceInstanceId, service.getId(), planId, organizationGuid, spaceGuid, null);
    }

    public ServiceInstance deleteServiceInstance(String id, String serviceId, String planId) {
        String instanceConfigPath = getInstanceConfigPath(id);
        s3client.deleteObject(new DeleteObjectRequest(brokerConfiguration.getSharedBucket(), instanceConfigPath));
        return new ServiceInstance(id, serviceId, planId, null, null, null);
    }

    public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
                                                               String serviceId, String planId, String appGuid) {

        Map<String, Object> credentials = new HashMap<String, Object>();
        credentials.put("bucket", brokerConfiguration.getSharedBucket());
        credentials.put("username", serviceId);
        credentials.put("access_key_id", brokerConfiguration.getSharedAccessKey());
        credentials.put("secret_access_key", brokerConfiguration.getSharedSecretKey());
        return new ServiceInstanceBinding(bindingId, serviceInstance.getId(), credentials, null, appGuid);
    }

    public ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
                                                               String serviceId, String planId) {
        return new ServiceInstanceBinding(bindingId, serviceInstance.getId(), null, null, null);
    }

    public ServiceInstance findServiceInstance(String id) {
        String instanceConfigPath = getInstanceConfigPath(id);
        S3Object s3Object = s3client.getObject(new GetObjectRequest(brokerConfiguration.getSharedBucket(), instanceConfigPath));
        if (s3Object != null) {
            return new ServiceInstance(id, null, PLAN_ID, null, null, null);
        } else {
            return null;
        }
    }
}
