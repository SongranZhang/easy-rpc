package com.linkedkeeper.easyrpc.test.client;

import com.linkedkeeper.easyrpc.client.RpcClient;
import com.linkedkeeper.easyrpc.client.RpcFuture;
import com.linkedkeeper.easyrpc.client.proxy.IAsyncObjectProxy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-client.xml")
public class HelloServiceTest {

    @Autowired
    private RpcClient rpcClient;

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
