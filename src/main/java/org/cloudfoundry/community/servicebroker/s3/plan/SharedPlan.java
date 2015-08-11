package org.cloudfoundry.community.servicebroker.s3.plan;

import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.s3.service.Iam;
import org.cloudfoundry.community.servicebroker.s3.service.S3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedPlan {
    private final Iam iam;
    private final S3 s3;
    public static final String PLAN_ID = "s3-shared-plan";

    public SharedPlan(Iam iam, S3 s3) {
        this.iam = iam;
        this.s3 = s3;
    }

    public static Plan getPlan() {
        Plan plan = new Plan(PLAN_ID, "shared",
                "An S3 plan providing a shared bucket with unlimited storage.", getPlanMetadata());
        return plan;
    }

    private static Map<String, Object> getPlanMetadata() {
        Map<String, Object> planMetadata = new HashMap<String, Object>();
        planMetadata.put("bullets", getPlanBullets());
        return planMetadata;
    }

    private static List<String> getPlanBullets() {
        return Arrays.asList("Shared S3 bucket", "Unlimited storage", "Unlimited number of objects");
    }

    public ServiceInstance createServiceInstance(ServiceDefinition service, String serviceInstanceId, String planId,
                                                 String organizationGuid, String spaceGuid) {
        return new ServiceInstance(serviceInstanceId, service.getId(), planId, organizationGuid, spaceGuid, null);
    }

    public ServiceInstance deleteServiceInstance(String id, String serviceId, String planId) {
        return new ServiceInstance(id, serviceId, planId, null, null, null);
    }
}
