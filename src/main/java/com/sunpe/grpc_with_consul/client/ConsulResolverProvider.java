package com.sunpe.grpc_with_consul.client;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import java.net.URI;

public class ConsulResolverProvider extends NameResolverProvider {
    private static final String SCHEME = "consul";

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 10;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        return new ConsulResolver(targetUri);
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }
}
