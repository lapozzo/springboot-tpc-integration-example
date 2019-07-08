package com.pozzo.tpcintegration;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.pozzo.tpcintegration.client.TcpClientGateway;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TpcClientApplicationTest {

	@Autowired
	private TcpClientGateway tcpClientGateway;

	@Test
	public void test() {
		for (int i = 0; i < 5; i++) {
			String serverResponse = tcpClientGateway.send("Test Message " + new Date());
			System.out.println(String.format("### Response %d: %s", i, serverResponse));
			Assert.assertNotNull(serverResponse);
		}
	}
}
