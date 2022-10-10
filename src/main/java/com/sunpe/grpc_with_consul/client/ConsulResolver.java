package com.sunpe.grpc_with_consul.client;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ConsulResolver extends NameResolver {

    private ConsulResolver() {
    }

    public ConsulResolver(URI target) {
        // target.Url - consul://127.0.0.1:8500/DEFAULT_GROUP/service_name
        this.target = target;
        String path = target.getPath();
        String[] ps = path.split("/");
        this.group = ps[1];
        this.serviceName = ps[2];
        this.client = new ConsulClient(target.getHost(), target.getPort());
    }

    @Override
    public String getServiceAuthority() {
        return this.target.getAuthority();
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void start(Listener listener) {
        HealthServicesRequest request = HealthServicesRequest
                .newBuilder()
                .setTag(this.group)
                .setPassing(true)
                .setQueryParams(QueryParams.Builder.builder().setIndex(this.lastIndex).build())
                .build();
        Response<List<HealthService>> response = this.client.getHealthServices(this.serviceName, request);
        this.lastIndex = response.getConsulIndex();
        List<HealthService> services = response.getValue();
        List<EquivalentAddressGroup> addressGroups = new ArrayList<>(services.size());

        for (HealthService service : services) {
            SocketAddress socketAddress = new InetSocketAddress(service.getService().getAddress(),
                    service.getService().getPort());
            Attributes attributes = Attributes.newBuilder().set(Attributes.Key.create("weight"), 1).build();
            EquivalentAddressGroup addressGroup = new EquivalentAddressGroup(socketAddress, attributes);
            addressGroups.add(addressGroup);
        }

        listener.onAddresses(addressGroups, Attributes.EMPTY);
    }

    private URI target;
    private String group;
    private String serviceName;
    private ConsulClient client;

    private long lastIndex;
}
