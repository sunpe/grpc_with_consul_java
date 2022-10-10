package com.sunpe.grpc_with_consul.server;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.sunpe.grpc_with_consul.common.Constant;
import com.sunpe.grpc_with_consul.server.proto.HealthCheckRequest;
import com.sunpe.grpc_with_consul.server.proto.HealthCheckResponse;
import com.sunpe.grpc_with_consul.server.proto.HealthGrpc;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrpcServer {

    private GrpcServer() {
    }

    public GrpcServer(String registry, String group, int port, List<BindableService> services) {
        this.group = group == null || "".equals(group) ? Constant.DEFAULT_GROUP : group;
        this.ip = getIp();
        this.port = port;
        String[] registries = registry.split(":");
        String registryHost = registries[0];
        int registryPort = 8500;
        if (registries.length == 2) {
            registryPort = Integer.parseInt(registries[1]);
        }

        this.consul = new ConsulClient(registryHost, registryPort);
        ServerBuilder<?> builder = ServerBuilder.forPort(port);
        builder.addService(new HealthChecker());

        for (BindableService service : services) {
            builder.addService(service);
        }

        this.server = builder.build();
    }

    public void start() throws IOException, InterruptedException {
        this.server.start();
        this.register();
        logger.warn("## grpc server start at [:{}]", this.server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("## shutting down gRPC server since JVM is shutting down");
            try {
                // this.deregister();
                this.stop();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
            System.err.println("## server shut down");
        }));
        server.awaitTermination();
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void register() {
        List<String> tags = new ArrayList<>(1);
        tags.add(this.group);

        NewService.Check check = new NewService.Check();
        check.setGrpc(this.ip + ":" + this.port);
        check.setInterval("2s");
        check.setTimeout("6s");
        check.setDeregisterCriticalServiceAfter("6s");

        List<ServerServiceDefinition> services = this.server.getServices();
        for (ServerServiceDefinition service : services) {
            String serviceName = service.getServiceDescriptor().getName();
            if (serviceName.equals(HealthGrpc.SERVICE_NAME)) {
                continue;
            }
            NewService s = new NewService();
            s.setId(serviceId(serviceName));
            s.setName(serviceName);
            s.setTags(tags);
            s.setPort(this.port);
            s.setCheck(check);

            this.consul.agentServiceRegister(s);
        }
    }

    private void deregister() {
        List<ServerServiceDefinition> services = this.server.getServices();
        for (ServerServiceDefinition service : services) {
            this.consul.agentServiceDeregister(serviceId(service.getServiceDescriptor().getName()));
        }
    }

    private String serviceId(String service) {
        return this.group + ":" + service + ":" + this.ip + ":" + this.port;
    }

    private String getIp() {
        String ip = "127.0.0.1";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // todo
        }
        return ip;
    }


    private String ip;
    private int port;
    private String group;
    private Server server;

    private ConsulClient consul;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    static class HealthChecker extends HealthGrpc.HealthImplBase {
        public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
            HealthCheckResponse response = HealthCheckResponse.newBuilder()
                    .setStatus(HealthCheckResponse.ServingStatus.SERVING)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        public void watch(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
        }
    }
}

