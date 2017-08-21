package cc.before30.mygrpc.samples.server.service;

import cc.before30.mygrpc.samples.protobuf.GreeterGrpc;
import cc.before30.mygrpc.samples.protobuf.HelloRequest;
import cc.before30.mygrpc.samples.protobuf.HelloResponse;
import cc.before30.mygrpc.server.GrpcService;
import io.grpc.stub.StreamObserver;

/**
 * User: before30 
 * Date: 2017. 8. 21.
 * Time: PM 8:45
 */
@GrpcService
public class GrpcGreeterService extends GreeterGrpc.GreeterImplBase {

	@Override public void sayHello2(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		for (int i=0; i<10; i++) {

			String message = request.getHello() + " : hello world : " + i;
			final HelloResponse.Builder responseBuilder = HelloResponse.newBuilder().setWelcomeMessage(message);

			responseObserver.onNext(responseBuilder.build());
		}
		responseObserver.onCompleted();
	}

	@Override public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		String message = request.getHello() + " : hello world : " + System.currentTimeMillis();
		final HelloResponse.Builder responseBuilder = HelloResponse.newBuilder().setWelcomeMessage(message);

		responseObserver.onNext(responseBuilder.build());
		responseObserver.onCompleted();
	}
}
