package ascelion.config.cdi;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import ascelion.config.annotations.ConfigValue;

class ConfigProducer {

	@Produces
	@ConfigValue("")
	Object produceValue(InjectionPoint ip) {
		return null;
	}
}
