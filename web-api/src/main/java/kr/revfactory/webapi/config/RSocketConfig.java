package kr.revfactory.webapi.config;

import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.metadata.BasicAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeTypeUtils;

@Configuration
public class RSocketConfig {
    @Bean
    public RSocketRequester accountRSocketRequester(RSocketStrategies rSocketStrategies) {
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("setup", "sha123");
        return RSocketRequester.builder()
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .rsocketStrategies(rSocketStrategies)
                .rsocketFactory(clientRSocketFactory -> {
                    clientRSocketFactory.frameDecoder(PayloadDecoder.ZERO_COPY);
                })
                .setupMetadata(credentials, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                .connect(TcpClientTransport.create(7000))
                .block();
    }

    @Bean
    public RSocketStrategies rsocketStrategies() {
        return RSocketStrategies.builder()
                .encoder(new BasicAuthenticationEncoder(), new Jackson2JsonEncoder())
                .decoder(new Jackson2JsonDecoder())
                .build();
    }
}
