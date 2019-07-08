package com.pozzo.tpcintegration.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;

@Configuration
@EnableIntegration
public class TcpServerConfig {

	@Value(value = "${tcp.server.port}")
	private int tcpServerPort;

	@Bean
	public IntegrationFlow commandServerFlow() {
		return IntegrationFlows.from(Tcp.inboundGateway(serverConnectionFactory()))
				.handle((payload, handlers) -> "Thread: " + Thread.currentThread().getName() + " Server response...")
				.get();
	}

	public AbstractServerConnectionFactory serverConnectionFactory() {
		TcpNioServerConnectionFactory tcpNetServerConnectionFactory = new TcpNioServerConnectionFactory(tcpServerPort);
		// socket will be used once
		tcpNetServerConnectionFactory.setSingleUse(true);
		tcpNetServerConnectionFactory.setSerializer(codec());
		tcpNetServerConnectionFactory.setDeserializer(codec());
		return tcpNetServerConnectionFactory;
	}

	public ByteArrayCrLfSerializer codec() {
		ByteArrayCrLfSerializer crLfSerializer = new ByteArrayCrLfSerializer();
		crLfSerializer.setMaxMessageSize(204800000);
		return crLfSerializer;
	}
}