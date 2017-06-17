# easy-rpc
RPC，即 Remote Procedure Call（远程过程调用），调用远程计算机上的服务，就像调用本地服务一样。RPC 可以很好的解耦系统，如 WebService 就是一种基于 Http 协议的 RPC。
EasyRpc 框架使用的一些技术所解决的问题：
* 通信：使用Netty作为通信框架。
* Spring：使用Spring配置服务，加载Bean。
* 动态代理：客户端使用代理模式透明化服务调用。
* 消息编解码：使用Protostuff序列化和反序列化消息。

## 服务端发布服务
一个服务接口
```java 
public interface HelloService {
 
    String hello(String name);
 
    String hello(Person person);
}
```
一个服务实现
```java 
public class HelloServiceImpl implements HelloService {
 
    public String hello(String name) {
        return "Hello! " + name;
    }
 
    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
```
spring-server.xml 配置文件
```xml 
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:easyrpc="http://www.linkedkeeper.com/schema/easyrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd
           http://www.linkedkeeper.com/schema/easyrpc
           http://www.linkedkeeper.com/schema/easyrpc/easyrpc.xsd">
 
    <bean id="helloService" 
        class="com.linkedkeeper.easyrpc.test.server.HelloServiceImpl"/>
 
    <easyrpc:provider id="HelloProvider" 
                      interface="com.linkedkeeper.easyrpc.test.client.HelloService"
                      alias="1.0" ref="helloService"/>
 
    <easyrpc:server id="rpcServer" protocol="easyrpc" port="18868"/>
 
</beans>
```

## 客户端调用服务
Junit Test
```java 
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-client.xml")
public class HelloServiceTest {

    @Autowired
    private RpcClient rpcClient = null;

    @Test
    public void helloTest1() {
        HelloService helloService = rpcClient.create(HelloService.class);
        String result = helloService.hello("World");
        System.out.println(result);
        Assert.assertEquals("Hello! World", result);
    }

    @Test
    public void helloTest2() {
        HelloService helloService = rpcClient.create(HelloService.class);
        Person person = new Person("Yong", "Huang");
        String result = helloService.hello(person);
        System.out.println(result.toString());
        Assert.assertEquals("Hello! Yong Huang", result);
    }

    @Test
    public void helloFutureTest1() throws ExecutionException, InterruptedException {
        IAsyncObjectProxy helloService = rpcClient.createAsync(HelloService.class);
        RpcFuture result = helloService.call("hello", "World");
        Assert.assertEquals("Hello! World", result.get());
    }

    @Test
    public void helloFutureTest2() throws ExecutionException, InterruptedException {
        IAsyncObjectProxy helloService = rpcClient.createAsync(HelloService.class);
        Person person = new Person("Yong", "Huang");
        RpcFuture result = helloService.call("hello", person);
        Assert.assertEquals("Hello! Yong Huang", result.get());
    }

    @After
    public void setTear() {
        if (rpcClient != null) {
            rpcClient.stop();
        }
    }
}
```
spring-client.xml 配置文件
```xml 
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:easyrpc="http://www.linkedkeeper.com/schema/easyrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.linkedkeeper.com/schema/easyrpc
            http://www.linkedkeeper.com/schema/easyrpc/easyrpc.xsd">

    <easyrpc:consumer id="rpcClient" url="127.0.0.1:18868"
                      interface="com.linkedkeeper.easyrpc.client.RpcClient"
                      alias="1.0" timeout="3"/>

</beans>
```