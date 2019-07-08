package com.pozzo.tpcintegration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.CachingClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Configuration
@EnableIntegration
public class TcpClientConfig {
	private static final Logger LOG = LoggerFactory.getLogger(TcpClientConfig.class);

	@Value(value = "${tcp.server.address}")
	private String tcpServerAddress;

	@Value(value = "${tcp.server.port}")
	private int tcpServerPort;

	@Value(value = "${tcp.client.timeout}")
	private int tcpClientTimeout;

	@Value("${tcp.client.poolSize}")
	private int tcpClientPoolSize;

	@Bean
	public DirectChannel tcpClientChannel() {
		return MessageChannels.direct().get();
	}

	@Bean
	public IntegrationFlow tcpClientToServerFlow() {
		return IntegrationFlows.from("tcpClientChannel")
				.handle(Tcp.outboundGateway(clientConnectionFactory()).remoteTimeout(tcpClientTimeout))
				.transform(Transformers.objectToString()).get();
	}

	@Bean
	public DirectChannel tcpClientErrorChannel() {
		return MessageChannels.direct().get();
	}

	@Bean
	public IntegrationFlow tcpClientErrorChannelFlow() {
		return IntegrationFlows.from("tcpClientErrorChannel").handle(new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				LOG.error(String.format("Error communicating with tcp server. Message sent: %s", message.getPayload()));
			}
		}).get();
	}

	public AbstractClientConnectionFactory clientConnectionFactory() {
		TcpNioClientConnectionFactory tcpNioClientConnectionFactory = new TcpNioClientConnectionFactory(
				tcpServerAddress, tcpServerPort);
		tcpNioClientConnectionFactory.setUsingDirectBuffers(true);
		tcpNioClientConnectionFactory.setSingleUse(true);
		tcpNioClientConnectionFactory.setSerializer(codec());
		tcpNioClientConnectionFactory.setDeserializer(codec());
		return new CachingClientConnectionFactory(tcpNioClientConnectionFactory, tcpClientPoolSize);
	}

	public ByteArrayCrLfSerializer codec() {
		ByteArrayCrLfSerializer crLfSerializer = new ByteArrayCrLfSerializer();
		crLfSerializer.setMaxMessageSize(204800000);
		return crLfSerializer;
	}
}