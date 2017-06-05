package com.linkedkeeper.easyrpc.test.server;

import com.linkedkeeper.easyrpc.test.client.HelloService;
import com.linkedkeeper.easyrpc.test.client.Person;

public class HelloServiceImpl implements HelloService {

    public String hello(String name) {
        return "Hello! " + name;
    }

    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
