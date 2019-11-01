package ascelion.config.cdi;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import ascelion.config.api.ConfigValue;

class ConfigValueProducer {

	@Produces
	@ConfigValue("")
	Object produceValue(InjectionPoint ip) {
		return null;
	}
}
