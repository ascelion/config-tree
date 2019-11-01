
package ascelion.config.convert;

import java.lang.reflect.Type;

import ascelion.config.spi.ConfigConverter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class WrappedConverter<C, T> implements ConfigConverter<C> {
	final Type type;
	final ConfigConverter<T> conv;
}
