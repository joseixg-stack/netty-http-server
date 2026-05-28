# Netty HTTP Server

一个基于 Netty 的简易 HTTP 服务器示例：

- 监听 `8080` 端口
- 打印每次请求的路径
- 使用责任链模式定义并执行 `GatewayFilter`
- 通过 `FilterChain` 统一管理和执行过滤器

## 运行

确保本机已安装 JDK 17 和 Maven，然后执行：

```powershell
mvn clean package
mvn exec:java
```

访问：

```powershell
curl http://localhost:8080/hello
```

终端会输出：

```text
Request path: /hello
```

## 过滤器接口

`GatewayFilter` 的核心方法：

```java
void filter(Request request, Response response, Chain chain) throws Exception;
```

自定义过滤器需要在处理完成后调用：

```java
chain.next(request, response);
```
