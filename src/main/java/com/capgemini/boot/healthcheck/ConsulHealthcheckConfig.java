package com.capgemini.boot.healthcheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * For most use cases only the service name should need to be supplied as the
 * default ought to suffice e.g. in config:
 * 
 * <pre class="code">
 * healthcheck:
 *   consul:
 *     serviceChecks:
 *       -
 *         serviceName: isit-kcom-xfer
 *       -
 *         serviceName: isit-cnx-xfer
 * </pre>
 * 
 * However some/all of the defaults may be overriden e.g.:
 * 
 * <pre class="code">
 * healthcheck:
 *   consul:
 *     serviceChecks:
 *       -
 *         serviceName: isit-kcom-xfer
 *         consulHost: consul-vip 
 *       -
 *         serviceName: isit-cnx-xfer
 *         consulHost: consul-vip
 * </pre>
 * 
 * Note the repetition of seemingly common config is necessary to support
 * independent ConsulHealthcheck beans for each service.
 */
@Component
@ConfigurationProperties(prefix = "healthcheck.consul")
public class ConsulHealthcheckConfig {

	private List<ServiceCheck> serviceChecks = new ArrayList<ServiceCheck>();

	public List<ServiceCheck> getServiceChecks() {
		return serviceChecks;
	}

	public void setServiceChecks(List<ServiceCheck> serviceChecks) {
		this.serviceChecks = serviceChecks;
	}

	public static class ServiceCheck {

		private String serviceName;

		private String consulHost = "localhost"; // default

		private int consulPort = 8500; // default

		private List<String> datacentreList = Arrays.asList(new String[] { "fast_cgzsdc01", "fast_cgzsdc02" }); // default

		private String consulUri = "http://%s:%s/v1/health/checks/%s?dc=%s"; // default

		public String getServiceName() {
			return serviceName;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}

		public String getConsulHost() {
			return consulHost;
		}

		public void setConsulHost(String consulHost) {
			this.consulHost = consulHost;
		}

		public int getConsulPort() {
			return consulPort;
		}

		public void setConsulPort(int consulPort) {
			this.consulPort = consulPort;
		}

		public List<String> getDatacentreList() {
			return datacentreList;
		}

		public void setDatacentreList(List<String> datacentreList) {
			this.datacentreList = datacentreList;
		}

		public String getConsulUri() {
			return consulUri;
		}

		public void setConsulUri(String consulUri) {
			this.consulUri = consulUri;
		}
	}

}
