package com.linkedkeeper.easyrpc.client.proxy;

import com.linkedkeeper.easyrpc.client.RpcFuture;

public interface IAsyncObjectProxy {

    RpcFuture call(String funcName, Object... args);

}
