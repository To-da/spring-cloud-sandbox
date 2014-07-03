package org.springframework.platform.netflix.endpoint;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;

/**
 * User: spencergibb
 * Date: 4/22/14
 * Time: 3:16 PM
 */
public class HystrixStreamEndpoint extends ServletWrappingEndpoint {

    public HystrixStreamEndpoint() {
        super(HystrixMetricsStreamServlet.class, "hystrixStream", "hystrix.stream", false, true);
    }
}
