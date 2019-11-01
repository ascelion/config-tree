package ascelion.config.spi;

import java.lang.reflect.Type;

public interface ConverterFactory {
	<T> ConfigConverter<T> get(Type type);
}
