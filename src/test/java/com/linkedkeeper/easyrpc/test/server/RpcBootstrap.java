package com.linkedkeeper.easyrpc.test.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RpcBootstrap {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring-server.xml");
    }
}
