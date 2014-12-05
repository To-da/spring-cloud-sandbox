package org.springframework.cloud.sample.frontend;

import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import com.netflix.hystrix.contrib.javanica.command.ObservableResult;
import com.netflix.niws.client.http.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sample.backend.Message;
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

    @Autowired
    HelloClient helloClient;

    @Autowired
    RestClient restClient;

    @HystrixCommand(fallbackMethod = "getDefaultMessage")
    public String getMessage() {
        return getMessageImpl();
    }

    @HystrixCommand(fallbackMethod = "getDefaultMessage")
    public String getMessage(String msg) {
        return helloClient.hello(msg).getBody();
    }

    //@HystrixCommand(fallbackMethod = "getDefaultMessage")
    public String getRibbonMessage() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri("http://localhost:7080/hello")
                .verb(HttpRequest.Verb.GET)
                .build();
        //TODO: wire spring message decoders to jersey client in RestClient
        HttpResponse response = restClient.execute(request);
        return response.getEntity(Message.class).getBody();
    }

    @HystrixCommand(fallbackMethod = "getDefaultMessage")
    public String getRestTemplateMessage() {
        Message message = restTemplate.getForObject("http://samplebackendservice/hello", Message.class);
        return message.getBody();
    }

    @HystrixCommand(fallbackMethod = "getDefaultMessage")
    public String sendMessage() {
        return helloClient.hello(new Message("World via POST")).getBody();
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

	@HystrixCommand(fallbackMethod = "getRxDefaultMessage")
	public Observable<String> getMessageRxFail() {
		throw new RuntimeException("getMessageRxFail failed on purpose");
	}

    @HystrixCommand(fallbackMethod = "getDefaultMessage")
    //TODO: setup hystrix to log errors by default
    public String getMessageFail() {
        throw new RuntimeException("I failed on purpose");
    }

    private String getMessageImpl() {
        return helloClient.hello().getBody();
    }

    private String getDefaultMessage(String msg) {
        return "World "+msg+" Default";
    }

    private String getDefaultMessage() {
        return "World Default";
    }

	private /*Observable<*/String/*>*/ getRxDefaultMessage() {
		/* fails with java.lang.ClassCastException: org.springframework.cloud.sample.frontend.HelloService$3 cannot be cast to java.lang.String
		return new ObservableResult<String>() {
			@Override
			public String invoke() {
				return "World Rx Default";
			}
		}; */
		return "World Rx Default";
	}
}
