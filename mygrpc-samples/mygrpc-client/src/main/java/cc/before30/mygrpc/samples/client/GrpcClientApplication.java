package cc.before30.mygrpc.samples.client;

import cc.before30.mygrpc.client.GrpcChannelBuilder;
import cc.before30.mygrpc.client.GrpcSource;
import cc.before30.mygrpc.samples.protobuf.GreeterGrpc;
import cc.before30.mygrpc.samples.protobuf.HelloRequest;
import cc.before30.mygrpc.samples.protobuf.HelloResponse;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Iterator;

/**
 * User: before30 
 * Date: 2017. 8. 21.
 * Time: PM 8:56
 */
@SpringBootApplication
@Slf4j
public class GrpcClientApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(GrpcClientApplication.class, args);
	}

	@Bean
	@ConfigurationProperties(prefix = "grpc.channels.local2")
	public GrpcSource local2GrpcSource() {
		return GrpcSource.create().build();
	}


	@Bean
	public Channel propertyLocal2(GrpcChannelBuilder builder) {
		return builder.grpcSource(local2GrpcSource()).build();
	}

	@Autowired
	@Qualifier("propertyLocal2")
	Channel local2;

	@Bean
	@ConfigurationProperties(prefix = "grpc.channels.local1")
	public GrpcSource local1GrpcSource() {
		return GrpcSource.create().build();
	}

	@Bean
	public Channel propertyLocal1(GrpcChannelBuilder builder) {
		return builder.grpcSource(local1GrpcSource()).build();
	}

	@Autowired
	@Qualifier("propertyLocal1")
	Channel local1;

	@Override public void run(String... args) throws Exception {
		GreeterGrpc.GreeterBlockingStub stub1 = GreeterGrpc.newBlockingStub(local2);
		HelloRequest request = HelloRequest.newBuilder().setHello("hello1").build();
		HelloResponse response = stub1.sayHello(request);
		log.info("response : {}", response.getWelcomeMessage());
		Iterator<HelloResponse> respIterator = stub1.sayHello2(request);
		respIterator.forEachRemaining(res -> {log.info(res.getWelcomeMessage());});

		GreeterGrpc.GreeterBlockingStub stub2 = GreeterGrpc.newBlockingStub(local1);
		HelloRequest request2 = HelloRequest.newBuilder().setHello("hello2").build();
		HelloResponse response2 = stub2.sayHello(request2);
		log.info("response : {}", response2.getWelcomeMessage());
		Iterator<HelloResponse> respIterator2 = stub2.sayHello2(request);
		respIterator2.forEachRemaining(res -> {log.info(res.getWelcomeMessage());});

	}
}
