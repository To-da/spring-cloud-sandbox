package org.springframework.platform.sample.frontend;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import com.netflix.hystrix.contrib.javanica.command.ObservableResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.platform.circuitbreaker.annotations.CircuitBreaker;
import org.springframework.platform.sample.backend.Message;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rx.Observable;

import java.util.concurrent.Future;

/**
 * Created by sgibb on 6/19/14.
 */
@Service
public class HelloService {
    @Autowired
    RestTemplate restTemplate;

    //@CircuitBreaker
    @HystrixCommand(fallbackMethod = "getDefaultMessage")
    public String getMessage() {
        return getMessageImpl();
    }

    @HystrixCommand(fallbackMethod = "getDefaultMessage")
    public Future<String> getMessageFuture() {
        return new AsyncResult<String>() {
            @Override
            public String invoke() {
                return getMessageImpl();
            }
        };
    }

    @HystrixCommand(fallbackMethod = "getDefaultMessage")
    public Observable<String> getMessageRx() {
        return new ObservableResult<String>() {
            @Override
            public String invoke() {
                return getMessageImpl();
            }
        };
    }

    @HystrixCommand(fallbackMethod = "getDefaultMessage")
    public String getMessageFail() {
        throw new RuntimeException("I failed on purpose");
    }

    private String getMessageImpl() {
        ResponseEntity<Message> message = restTemplate.getForEntity("http://localhost:7080/hello", Message.class);
        return message.getBody().getBody();
    }

    private String getDefaultMessage() {
        return "World Default";
    }
}
