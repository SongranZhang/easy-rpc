# easy-rpc
RPC，即 Remote Procedure Call（远程过程调用），调用远程计算机上的服务，就像调用本地服务一样。RPC 可以很好的解耦系统，如 WebService 就是一种基于 Http 协议的 RPC。
EasyRpc 框架使用的一些技术所解决的问题：
* 通信：使用Netty作为通信框架。
* Spring：使用Spring配置服务，加载Bean，扫描注解。
* 动态代理：客户端使用代理模式透明化服务调用。
* 消息编解码：使用Protostuff序列化和反序列化消息。

