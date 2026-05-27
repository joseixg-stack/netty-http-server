# Netty HTTP Server

一个基于 Netty 的简易 HTTP 服务器示例：

- 监听 `8080` 端口
- 打印每次请求的路径
- 使用责任链模式定义并执行 `Filter`

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

