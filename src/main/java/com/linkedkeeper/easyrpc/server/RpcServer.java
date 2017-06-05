package com.linkedkeeper.easyrpc.server;

import com.linkedkeeper.easyrpc.codec.RpcDecoder;
import com.linkedkeeper.easyrpc.codec.RpcEncoder;
import com.linkedkeeper.easyrpc.codec.RpcRequest;
import com.linkedkeeper.easyrpc.codec.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServer implements ApplicationContextAware, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));

    private String serverAddress;
    private Map<String, Object> handleMap = new HashMap();

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                handleMap.put(interfaceName, serviceBean);
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                .addLast(new RpcDecoder(RpcRequest.class))
                                .addLast(new RpcEncoder(RpcResponse.class))
                                .addLast(new RpcHandler(handleMap));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        String[] array = serverAddress.split(":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);

        ChannelFuture future = bootstrap.bind(host, port).sync();
        ChannelFuture channelFuture = future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    LOGGER.info("Server have success bind to " + serverAddress);
                } else {
                    LOGGER.error("Server fail bind to " + serverAddress);
                    throw new Exception("Server start fail !", future.cause());
                }
            }
        });

        try {
            channelFuture.await(5000, TimeUnit.MILLISECONDS);
            if (channelFuture.isSuccess()) {
                LOGGER.info("start easy rpc server success.");
            }

        } catch (InterruptedException e) {
            LOGGER.error("start easy rpc occur InterruptedException!", e);
        }
    }

    public void destroy() throws Exception {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }
}
