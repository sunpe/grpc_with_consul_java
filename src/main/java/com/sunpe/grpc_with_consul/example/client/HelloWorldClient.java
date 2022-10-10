package com.sunpe.grpc_with_consul.example.client;

import com.sunpe.grpc_with_consul.client.GrpcClient;
import com.sunpe.grpc_with_consul.example.proto.GreeterGrpc;
import com.sunpe.grpc_with_consul.example.proto.HelloRequest;
import com.sunpe.grpc_with_consul.example.proto.HelloResponse;
import io.grpc.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class HelloWorldClient {
    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public HelloWorldClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void greet(String name) {
        logger.info("Will try to greet " + name + " ...");
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloResponse response = blockingStub.sayHello(request);
        logger.info("Greeting: " + response.getMessage());
    }

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = new GrpcClient("consul://127.0.0.1:8500/DEFAULT_GROUP/helloworld.Greeter");
        HelloWorldClient client = new HelloWorldClient(channel);
        client.greet("world");
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

}
