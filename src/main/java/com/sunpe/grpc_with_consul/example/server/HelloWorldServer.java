package com.sunpe.grpc_with_consul.example.server;

import com.sunpe.grpc_with_consul.example.proto.GreeterGrpc;
import com.sunpe.grpc_with_consul.example.proto.HelloRequest;
import com.sunpe.grpc_with_consul.example.proto.HelloResponse;
import com.sunpe.grpc_with_consul.server.GrpcServer;
import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelloWorldServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        List<BindableService> services = new ArrayList<>();
        services.add(new GreeterImpl());

        GrpcServer server = new GrpcServer("127.0.0.1:8500", "", 8000, services);
        server.start();
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloResponse> responseObserver) {
            HelloResponse reply = HelloResponse.newBuilder().setMessage("Hello " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
