package ascelion.config.cdi;

import javax.enterprise.util.AnnotationLiteral;

import ascelion.config.api.ConfigValue;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class ConfigValueLiteral extends AnnotationLiteral<ConfigValue> implements ConfigValue {

	static ConfigValueLiteralBuilder builder(ConfigValue annotation) {
		return new ConfigValueLiteralBuilder()
				.required(annotation.required())
				.usePrefix(annotation.usePrefix())
				.type(annotation.type());
	}

	private final String value;
	private final boolean required;
	private final boolean usePrefix;
	private final Class<?> type;

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
