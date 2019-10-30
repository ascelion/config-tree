package ascelion.config.cdi;

import java.lang.reflect.Type;
import java.util.function.Supplier;

import javax.enterprise.util.AnnotationLiteral;

import ascelion.config.annotations.ConfigValue;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ConfigValueLiteral extends AnnotationLiteral<ConfigValue> implements ConfigValue {

	static Class<?> actualType(Type type, ConfigValue annotation) {
		return annotation.type() != Object.class ? annotation.type() : (Class<?>) type;
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows
	static <T> T createType(ConfigValue annotation, Supplier<T> sup) {
		return annotation.type() != Object.class ? (T) annotation.type().newInstance() : sup.get();
	}

	private final String value;
	private final boolean required;
	private final boolean usePrefix;
	private final Class<?> type;

	ConfigValueLiteral(String value, ConfigValue config) {
		this(value, config.required(), config.usePrefix(), config.type());
	}

	@Override
	public String value() {
		return this.value;
	}

	@Override
	public boolean required() {
		return this.required;
	}

	@Override
	public boolean usePrefix() {
		return this.usePrefix;
	}

	@Override
	public Class<?> type() {
		return this.type;
	}
}
