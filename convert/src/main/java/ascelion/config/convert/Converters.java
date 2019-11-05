package ascelion.config.convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConverterFactory;

import static io.leangen.geantyref.GenericTypeReflector.getTypeParameter;
import static java.lang.String.format;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class Converters implements ConverterFactory {
	static private final Logger LOG = LoggerFactory.getLogger(Converters.class);
	static final TypeVariable<? extends Class<?>> CV_TYPE = ConfigConverter.class.getTypeParameters()[0];
	static private final String[] CREATE_METHODS = { "valueOf", "parse", "create", "from", "fromValue", "of" };

	private final Map<Type, PrioritizedCollection<ConfigConverter<?>>> cached = new HashMap<>();
	private final PrioritizedCollection<ConverterFactory> factories = new PrioritizedCollection<>();

	public Converters() {
		addFunction(String.class, UnaryOperator.identity());

		addFunction(boolean.class, Boolean::valueOf);
		addFunction(byte.class, Byte::parseByte);
		addFunction(short.class, Short::parseShort);
		addFunction(int.class, Integer::parseInt);
		addFunction(long.class, Long::parseLong);
		addFunction(float.class, Float::parseFloat);
		addFunction(double.class, Double::parseDouble);

		addFunction(Boolean.class, Boolean::valueOf);
		addFunction(Byte.class, Byte::parseByte);
		addFunction(Short.class, Short::parseShort);
		addFunction(Integer.class, Integer::parseInt);
		addFunction(Long.class, Long::parseLong);
		addFunction(Float.class, Float::parseFloat);
		addFunction(Double.class, Double::parseDouble);
	}

	public void register(ConfigConverter<?> converter) {
		final Class<? extends ConfigConverter> type = converter.getClass();

		this.cached.compute(getTypeParameter(type, CV_TYPE), (t, s) -> addConverter(t, s, converter));
	}

	public void register(ConverterFactory factory) {
		LOG.debug("Registering {}", factory.getClass().getName());

		this.factories.add(factory);
	}

	@Override
	public <T> ConfigConverter<T> get(Type type) {
		return (ConfigConverter<T>) this.cached
				.computeIfAbsent(type, t -> addConverter(t, null, inferConverter(t), Integer.MAX_VALUE))
				.head();
	}

	private <T> void addFunction(Type type, Function<String, T> func) {
		final ConfigConverter<T> conv = node -> node.getValue().map(func);

		this.cached.compute(type, (t, s) -> addConverter(t, s, conv, Integer.MAX_VALUE));
	}

	private PrioritizedCollection<ConfigConverter<?>> addConverter(Type type, PrioritizedCollection<ConfigConverter<?>> col, ConfigConverter<?> cvt) {
		if (col == null) {
			col = new PrioritizedCollection<>();
		}

		LOG.debug("Registering {} for {}", cvt.getClass().getName(), type.getTypeName());

		col.add(cvt);

		return col;
	}

	private PrioritizedCollection<ConfigConverter<?>> addConverter(Type type, PrioritizedCollection<ConfigConverter<?>> col, ConfigConverter<?> cvt, int pri) {
		if (col == null) {
			col = new PrioritizedCollection<>();
		}

		LOG.debug("Registering {} for {}", cvt.getClass().getName(), type.getTypeName());

		col.add(cvt, pri);

		return col;
	}

	private <T> ConfigConverter<T> inferConverter(Type type) {
		ConfigConverter<T> conv;

		if (type instanceof ParameterizedType) {
			if ((conv = parameterizedTypeConverter((ParameterizedType) type)) != null) {
				return conv;
			}
		}
		if (type instanceof GenericArrayType) {
			if ((conv = genericArrayConverter((GenericArrayType) type)) != null) {
				return conv;
			}
		}
		if (type instanceof Class) {
			if ((conv = classConverter((Class<T>) type)) != null) {
				return conv;
			}
		}
		for (final ConverterFactory factory : this.factories) {
			if ((conv = factory.get(type)) != null) {
				return conv;
			}
		}

		throw new ConfigException(format("NO WAY to construct a %s", type.getTypeName()));
	}

	private <T> ConfigConverter<T> parameterizedTypeConverter(ParameterizedType type) {
		final Type rawType = type.getRawType();

		if (!(rawType instanceof Class)) {
			return null;
		}

		final Class<?> rawClass = (Class<?>) rawType;

		if (Collection.class.isAssignableFrom(rawClass)) {
			final Type actual = type.getActualTypeArguments()[0];
			final ConfigConverter<?> conv = get(actual);
			Supplier<? extends Collection<?>> sup = null;

			if (rawClass.isInterface()) {
				if (SortedSet.class == rawClass) {
					if (rawClass.isAssignableFrom(TreeSet.class)) {
						sup = TreeSet::new;
					}
				} else if (Set.class == rawClass) {
					if (rawClass.isAssignableFrom(HashSet.class)) {
						sup = HashSet::new;
					}
				} else {
					if (rawClass.isAssignableFrom(ArrayList.class)) {
						sup = ArrayList::new;
					}
				}
			} else {
				sup = () -> (Collection) newInstance(rawClass);
			}

			if (sup != null) {
				return new CollectionConverter(sup, actual, conv);
			}

			return null;
		}
		if (Map.class.isAssignableFrom(rawClass)) {
			final Type actual = type.getActualTypeArguments()[1];
			final ConfigConverter<?> conv = get(actual);
			Supplier<? extends Map<?, ?>> sup = null;

			if (rawClass.isInterface()) {
				if (SortedMap.class == rawClass) {
					if (rawClass.isAssignableFrom(TreeMap.class)) {
						sup = TreeMap::new;
					}
				} else {
					if (rawClass.isAssignableFrom(HashMap.class)) {
						sup = HashMap::new;
					}
				}
			} else {
				sup = () -> (Map) newInstance(rawClass);
			}

			return new MapConverter(sup, actual, conv);
		}

		return null;
	}

	@SneakyThrows
	private <T> T newInstance(Class<T> type) {
		return type.newInstance();
	}

	private <T> ConfigConverter<T> genericArrayConverter(GenericArrayType type) {
		final Type compType = type.getGenericComponentType();

		return new ArrayConverter(compType, get(compType));
	}

	private <T> ConfigConverter<T> classConverter(Class<T> type) {
		if (type.isEnum()) {
			return new EnumConverter(type);
		}
		if (type.isArray()) {
			final Class<?> compType = type.getComponentType();

			if (compType.isPrimitive()) {
				return new PrimitiveArrayConverter(compType, get(compType));
			} else {
				return new ArrayConverter(compType, get(compType));
			}
		}
		if (type.isInterface()) {
			return new InterfaceConverter<>(type, this);
		}

		return fromClass(type);
	}

	private <T> ConfigConverter<T> fromClass(Class<T> type) {
		ConfigConverter<T> c;

		if ((c = fromConstructor(type, String.class)) != null) {
			return c;
		}
		if ((c = fromConstructor(type, CharSequence.class)) != null) {
			return c;
		}

		for (final String name : CREATE_METHODS) {
			if ((c = fromMethod(type, name, String.class)) != null) {
				return c;
			}
			if ((c = fromMethod(type, name, CharSequence.class)) != null) {
				return c;
			}
		}

		return null;
	}

	private <T> ConfigConverter<T> fromConstructor(Class<T> type, Class<? extends CharSequence> paramType) {
		final Constructor<T> c;

		try {
			c = type.getDeclaredConstructor(paramType);
		} catch (final NoSuchMethodException e) {
			return null;
		}

		return new ConfigConverter<T>() {
			@Override
			@SneakyThrows
			public Optional<T> convert(ConfigNode node) {
				final String value = node.getValue().orElse(null);

				if (value == null) {
					return Optional.empty();
				}

				c.setAccessible(true);

				return Optional.of(c.newInstance(value));
			}
		};
	}

	private <T> ConfigConverter<T> fromMethod(Class<T> type, String name, Class<? extends CharSequence> paramType) {
		final Method m;

		try {
			m = type.getDeclaredMethod(name, paramType);
		} catch (final NoSuchMethodException e) {
			return null;
		}

		if (!Modifier.isStatic(m.getModifiers())) {
			return null;
		}

		return new ConfigConverter<T>() {
			@SuppressWarnings("unchecked")
			@Override
			@SneakyThrows
			public Optional<T> convert(ConfigNode node) {
				final String value = node.getValue().orElse(null);

				if (value == null) {
					return Optional.empty();
				}

				m.setAccessible(true);

				return Optional.of((T) m.invoke(null, value));
			}
		};
	}
}
