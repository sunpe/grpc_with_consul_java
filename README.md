# grpc_with_consul_java

基于 consul 服务注册发现的 grpc 客户端/服务端。

## consul target 定义

grpc_with_consul_java 使用 consul 作为服务注册中心，服务 target 定义为 `consul://{consul_address}/{group}/{service_name}`。 比如 `consul://127.0.0.1:8500/DEFAULT_GROUP/helloworld.Greeter`

## client

基于 consul 的 grpc 客户端。

### GrpcClient

创建 grpc 客户端链接。target 参数支持默认的 ip+port，或consul scheme。 比如

```
ManagedChannel channel = new GrpcClient("127.0.0.1:8000");
ManagedChannel channel = new GrpcClient("consul://127.0.0.1:8500/DEFAULT_GROUP/helloworld.Greeter");
```

## server

基于 consul 的 grpc 服务端。

### GrpcServer

创建 grpc server，比如：

```
List<BindableService> services = new ArrayList<>();
services.add(new GreeterImpl());

GrpcServer server = new GrpcServer("127.0.0.1:8500", "", 8000, services);
server.start();
```

## example 说明

- 在本地启动 consul

```shell
consul agent --dev
```

- 运行服务端
- 运行客户端
