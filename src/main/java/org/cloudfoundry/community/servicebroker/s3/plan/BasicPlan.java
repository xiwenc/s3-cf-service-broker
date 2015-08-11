package org.cloudfoundry.community.servicebroker.s3.plan;

import org.cloudfoundry.community.servicebroker.model.Plan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicPlan {
    public static Plan getPlan() {
        Plan plan = new Plan("s3-basic-plan", "Basic S3 Plan",
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
}
