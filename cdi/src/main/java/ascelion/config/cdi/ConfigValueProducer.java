package ascelion.config.cdi;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import ascelion.config.api.ConfigRoot;
import ascelion.config.api.ConfigValue;

class ConfigValueProducer {
	@Inject
	private ConfigRoot root;

	@ConfigValue("")
	Object produceValue(InjectionPoint ip) {
		final ConfigValue annotation = ip.getAnnotated().getAnnotation(ConfigValue.class);
		final String property = annotation.value();
		final Type type = ip.getType();
		final Optional<Object> opt = this.root.getValue(property, type);

		if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == Optional.class) {
			return opt;
		}

		if (annotation.required()) {
			return opt.orElseThrow(() -> new NoSuchElementException("Reference to undefined property " + property));
		} else {
			return opt.orElseGet(() -> Primitives.toDefault(type));
		}
	}
}
