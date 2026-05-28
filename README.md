Netty Gateway
一个基于 Java Netty 构建的轻量级 API 网关。旨在学习高性能网络编程与微服务架构的流量调度原理。

🚀 项目特性
高性能： 基于 Netty 异步非阻塞 IO 架构。

责任链模式： 灵活的插件系统（Filter Chain），支持自定义鉴权、限流等功能。

动态路由： 支持基于路径前缀的请求转发。

🏗️ 架构概览
### 架构概览
```mermaid
graph LR
    Client[客户端] --> Gateway[API 网关]
    subgraph 网关内部
        Filter[Filter Chain] --> Router[路由转发]
    end
    Gateway --> Filter
    Router --> Service[后端服务]

🛠️ 技术栈
核心框架： Netty 4.1.x

构建工具： Apache Maven

开发语言： Java 17+

⚡ 快速开始
克隆项目： git clone [https://github.com/joseixg-stack/netty-http-server.git]

构建项目： mvn clean package

运行网关： java -jar target/gateway-1.0.jar

验证： 发送请求 http://localhost:8080/hello

📝 开发进度 (Roadmap)
[x] 基于 Netty 的 HTTP 基础服务器搭建

[ ] 实现责任链 Filter 接口

[ ] 完成基础路由转发逻辑

[ ] 集成 JWT 鉴权插件

💡 通过这个项目，深入理解Netty 的 EventLoop 模型以及如何优雅地处理 HTTP 报文。)
