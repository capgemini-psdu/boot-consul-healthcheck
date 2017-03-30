package com.capgemini.boot.healthcheck;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.web.client.RestTemplate;

/**
 * A Spring Boot HealthIndicator that can be configured to monitor the health of
 * a single Consul service. Where multiple services need to be monitored
 * additional ConsulHealthcheck beans should be registered.
 */
public class ConsulHealthcheck extends AbstractHealthIndicator {

	private RestTemplate template = new RestTemplate();

	private ConsulHealthcheckConfig.ServiceCheck config;

	public ConsulHealthcheck(ConsulHealthcheckConfig.ServiceCheck config) {
		this.config = config;
	}

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		boolean serviceUp = false;
		for (String dc : config.getDatacentreList()) {
			String response = template.getForObject(buildUri(config.getServiceName(), dc), String.class);
			if (response.contains("200 OK")) {
				serviceUp = true;
				break;
			}
		}
		if (serviceUp) {
			builder.up();
		} else {
			builder.down().withDetail(config.getServiceName(), "All nodes down");
		}
	}

	private String buildUri(String service, String dc) {
		return String.format("http://%s:%s/v1/health/checks/%s?dc=%s", config.getConsulHost(), config.getConsulPort(),
				service, dc);
	}
}
