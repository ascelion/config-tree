
package ascelion.config.convert;

import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConverterFactory;

import java.lang.reflect.Proxy;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class InterfaceConverter<T> implements ConfigConverter<T> {
	private final Class<T> type;
	private final ConverterFactory converters;

	@SuppressWarnings("unchecked")
	@Override
	public Optional<T> convert(ConfigNode node) {
		final Class<?>[] types = new Class[] { this.type };
		final ClassLoader cld = this.type.getClassLoader();
		final T instance = (T) Proxy.newProxyInstance(cld, types, new InterfaceValue(this.type, node, this.converters));

		return Optional.of(instance);
	}
}
