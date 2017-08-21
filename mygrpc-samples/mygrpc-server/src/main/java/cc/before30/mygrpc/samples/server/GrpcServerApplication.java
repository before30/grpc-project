package cc.before30.mygrpc.samples.server;

import cc.before30.mygrpc.samples.protobuf.GreeterGrpc;
import cc.before30.mygrpc.samples.protobuf.HelloRequest;
import cc.before30.mygrpc.samples.protobuf.HelloResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Iterator;

/**
 * User: before30 
 * Date: 2017. 8. 21.
 * Time: PM 8:39
 */
@SpringBootApplication
@Slf4j
public class GrpcServerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(GrpcServerApplication.class, args);
	}

	@Override public void run(String... args) throws Exception {
		log.info("*** hello world ***");
		ManagedChannel managedChannel = ManagedChannelBuilder
			.forAddress("localhost", 8888)
			.usePlaintext(true)
			.build();
		GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(managedChannel);
		HelloRequest request = HelloRequest.newBuilder().setHello("hello1").build();
		HelloResponse response = stub.sayHello(request);
		log.info("response : {}", response.getWelcomeMessage());

		Iterator<HelloResponse> respIterator = stub.sayHello2(request);
		respIterator.forEachRemaining(res -> {log.info(res.getWelcomeMessage());});
	}
}
