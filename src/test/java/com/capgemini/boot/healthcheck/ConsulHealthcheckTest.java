package com.capgemini.boot.healthcheck;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import com.capgemini.boot.healthcheck.ConsulHealthcheck;
import com.capgemini.boot.healthcheck.ConsulHealthcheckConfig;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class ConsulHealthcheckTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8500); // Port that Consul would normally be on
	
	private String successResponse = "[{\"Node\":\"DEVU0000843\",\"CheckID\":\"service:isit-kcom-xfer\",\"Name\":\"Service 'isit-kcom-xfer' check\",\"Status\":\"passing\",\"Notes\":\"\",\"Output\":\"HTTP GET http://localhost:14004/health: 200 OK Output: {\"status\":\"UP\"}\",\"ServiceID\":\"isit-kcom-xfer\",\"ServiceName\":\"isit-kcom-xfer\"}]";

	private String downResponse = "[{\"Node\":\"DEVU0000843\",\"CheckID\":\"service:isit-kcom-xfer\",\"Name\":\"Service 'isit-kcom-xfer' check\",\"Status\":\"critical\",\"Notes\":\"\",\"Output\":\"HTTP GET http://localhost:14004/health: 503 Service Unavailable Output: {\"status\":\"DOWN\"}\",\"ServiceID\":\"isit-kcom-xfer\",\"ServiceName\":\"isit-kcom-xfer\"}]";
	
	private ConsulHealthcheck consulHealthcheck;
	
	private ConsulHealthcheckConfig config;
	
	private ConsulHealthcheckConfig.ServiceCheck serviceCheck;
	
	@Before
	public void setUp() {
		initMocks(this);
		config = new ConsulHealthcheckConfig();
		serviceCheck = new ConsulHealthcheckConfig.ServiceCheck();
		serviceCheck.setServiceName("isit-kcom-xfer");
		config.getServiceChecks().add(serviceCheck);
		consulHealthcheck = new ConsulHealthcheck(serviceCheck);
	}

	@Test
	public void testBothUp() throws Exception {
		// Set up wiremock stub to respond to the rest template
		stubFor(get(urlEqualTo("/v1/health/checks/isit-kcom-xfer?dc=fast_cgzsdc01"))
				.willReturn(aResponse()
				.withStatus(200)
				.withBody(successResponse)));
		stubFor(get(urlEqualTo("/v1/health/checks/isit-kcom-xfer?dc=fast_cgzsdc02"))
				.willReturn(aResponse()
				.withStatus(200)
				.withBody(successResponse)));
		
		Health health = consulHealthcheck.health();
		
		assertEquals(Status.UP, health.getStatus());		
	}

	@Test
	public void testFirstDown() throws Exception {
		// Set up wiremock stub to respond to the rest template
		stubFor(get(urlEqualTo("/v1/health/checks/isit-kcom-xfer?dc=fast_cgzsdc01"))
				.willReturn(aResponse()
				.withStatus(200)
				.withBody(downResponse)));
		stubFor(get(urlEqualTo("/v1/health/checks/isit-kcom-xfer?dc=fast_cgzsdc02"))
				.willReturn(aResponse()
				.withStatus(200)
				.withBody(successResponse)));
		
		Health health = consulHealthcheck.health();
		
		assertEquals(Status.UP, health.getStatus());		
	}

	@Test
	public void testSecondDown() throws Exception {
		// Set up wiremock stub to respond to the rest template
		stubFor(get(urlEqualTo("/v1/health/checks/isit-kcom-xfer?dc=fast_cgzsdc01"))
				.willReturn(aResponse()
				.withStatus(200)
				.withBody(successResponse)));
		stubFor(get(urlEqualTo("/v1/health/checks/isit-kcom-xfer?dc=fast_cgzsdc02"))
				.willReturn(aResponse()
				.withStatus(200)
				.withBody(downResponse)));
		
		Health health = consulHealthcheck.health();
		
		assertEquals(Status.UP, health.getStatus());		
	}

	@Test
	public void testBothDown() throws Exception {
		// Set up wiremock stub to respond to the rest template
		stubFor(get(urlEqualTo("/v1/health/checks/isit-kcom-xfer?dc=fast_cgzsdc01"))
				.willReturn(aResponse()
				.withStatus(200)
				.withBody(downResponse)));
		stubFor(get(urlEqualTo("/v1/health/checks/isit-kcom-xfer?dc=fast_cgzsdc02"))
				.willReturn(aResponse()
				.withStatus(200)
				.withBody(downResponse)));
		
		Health health = consulHealthcheck.health();
		
		assertEquals(Status.DOWN, health.getStatus());		
	}

	@Test
	public void testOnlyOneDown() throws Exception {
		serviceCheck.setDatacentreList(Arrays.asList(new String[]{"fast_cgzsdc01"}));
		// Set up wiremock stub to respond to the rest template
		stubFor(get(urlEqualTo("/v1/health/checks/isit-kcom-xfer?dc=fast_cgzsdc01"))
				.willReturn(aResponse()
				.withStatus(200)
				.withBody(downResponse)));
		
		Health health = consulHealthcheck.health();
		
		assertEquals(Status.DOWN, health.getStatus());		
	}

	@Test
	public void testInvalidDc() throws Exception {
		serviceCheck.setDatacentreList(Arrays.asList(new String[]{"foo"}));
		// Set up wiremock stub to respond to the rest template
		stubFor(get(urlEqualTo("/v1/health/checks/isit-kcom-xfer?dc=foo"))
				.willReturn(aResponse()
				.withStatus(200)
				.withBody("No path to datacenter")));
		
		Health health = consulHealthcheck.health();
		
		assertEquals(Status.DOWN, health.getStatus());		
	}


}