package com.sunpe.grpc_with_consul.client;

import io.grpc.*;

import java.util.concurrent.TimeUnit;

public class GrpcClient extends ManagedChannel {

    private GrpcClient() {
    }

    public GrpcClient(String target) {
        NameResolverRegistry.getDefaultRegistry().register(new ConsulResolverProvider());
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    }

    @Override
    public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(MethodDescriptor<RequestT, ResponseT> methodDescriptor, CallOptions callOptions) {
        return this.channel.newCall(methodDescriptor, callOptions);
    }

    @Override
    public String authority() {
        return this.channel.authority();
    }

    @Override
    public ManagedChannel shutdown() {
        return this.channel.shutdown();
    }

    @Override
    public boolean isShutdown() {
        return this.channel.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.channel.isTerminated();
    }

    @Override
    public ManagedChannel shutdownNow() {
        return this.channel.shutdownNow();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.channel.awaitTermination(timeout, unit);
    }

    private ManagedChannel channel;
}
