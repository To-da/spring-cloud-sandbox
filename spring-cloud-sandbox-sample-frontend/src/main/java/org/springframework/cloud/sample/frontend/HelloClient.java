package org.springframework.cloud.sample.frontend;

import org.springframework.cloud.sample.backend.Message;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by sgibb on 6/26/14.
 */
public interface HelloClient {
    @RequestMapping(method = RequestMethod.GET, value = "/hello")
    Message hello();

    @RequestMapping(method = RequestMethod.GET, value = "/hello")
    Message hello(@RequestParam("msg") String msg);

    @RequestMapping(method = RequestMethod.POST, value = "/hello", consumes = "application/json")
    Message hello(Message message);
}
