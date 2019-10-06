package kr.revfactory.account.config;

import io.rsocket.frame.decoder.PayloadDecoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.rsocket.context.RSocketServerBootstrap;
import org.springframework.boot.rsocket.netty.NettyRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.boot.rsocket.server.RSocketServerFactory;
import org.springframework.boot.rsocket.server.ServerRSocketFactoryProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

import java.net.InetAddress;
import java.util.stream.Collectors;

@Configuration
public class RSocketConfiguration {
    @Bean
    ReactorResourceFactory reactorResourceFactory() {
        return new ReactorResourceFactory();
    }

    @Bean
    RSocketServerFactory rSocketServerFactory(ReactorResourceFactory resourceFactory,
                                              ObjectProvider<ServerRSocketFactoryProcessor> customizers) throws Exception {
        NettyRSocketServerFactory factory = new NettyRSocketServerFactory();
        factory.setResourceFactory(resourceFactory);
        factory.setTransport(RSocketServer.Transport.TCP);
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(InetAddress.getByName("localhost")).to(factory::setAddress);
        map.from(7000).to(factory::setPort);
        factory.setServerProcessors(customizers.orderedStream().collect(Collectors.toList()));
        return factory;
    }

    @Bean
    RSocketServerBootstrap rSocketServerBootstrap(RSocketServerFactory rSocketServerFactory,
                                                  RSocketMessageHandler rSocketMessageHandler) {
        return new RSocketServerBootstrap(rSocketServerFactory, rSocketMessageHandler.responder());
    }

    @Bean
    ServerRSocketFactoryProcessor frameDecoderServerFactoryProcessor(
            RSocketMessageHandler rSocketMessageHandler, PayloadSocketAcceptorInterceptor rsocketInterceptor) {
        return (serverRSocketFactory) -> {
            if (rSocketMessageHandler.getRSocketStrategies()
                    .dataBufferFactory() instanceof NettyDataBufferFactory) {
                serverRSocketFactory.frameDecoder(PayloadDecoder.ZERO_COPY);
            }
            return serverRSocketFactory.addSocketAcceptorPlugin(rsocketInterceptor);
        };
    }
}
