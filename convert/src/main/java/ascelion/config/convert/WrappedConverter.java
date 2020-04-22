
package ascelion.config.convert;

import ascelion.config.spi.ConfigConverter;

import java.lang.reflect.Type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class WrappedConverter<C, T> implements ConfigConverter<C> {
	final Type type;
	final ConfigConverter<T> conv;
}
