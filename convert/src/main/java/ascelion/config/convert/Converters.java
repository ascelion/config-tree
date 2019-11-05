package ascelion.config.convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

import javax.annotation.Priority;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConverterFactory;

import static io.leangen.geantyref.GenericTypeReflector.getTypeParameter;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class Converters implements ConverterFactory {
	static final TypeVariable<? extends Class<?>> CV_TYPE = ConfigConverter.class.getTypeParameters()[0];

	static private final String[] CREATE_METHODS = { "valueOf", "parse", "create", "from", "fromValue" };

	@RequiredArgsConstructor
	static class ConverterInfo<T> implements Comparable<ConverterInfo<T>> {
		private final ConfigConverter<T> instance;
		private final int priority;

		@Override
		public int compareTo(ConverterInfo<T> that) {
			return Integer.compare(this.priority, that.priority);
		}

		ConfigConverter<T> instance() {
			return this.instance;
		}
	}

	private final Map<Type, SortedSet<ConverterInfo<?>>> cached = new HashMap<>();

	public Converters() {
		add(String.class, UnaryOperator.identity());

		add(boolean.class, Boolean::valueOf);
		add(byte.class, Byte::parseByte);
		add(short.class, Short::parseShort);
		add(int.class, Integer::parseInt);
		add(long.class, Long::parseLong);
		add(float.class, Float::parseFloat);
		add(double.class, Double::parseDouble);

		add(Boolean.class, Boolean::valueOf);
		add(Byte.class, Byte::parseByte);
		add(Short.class, Short::parseShort);
		add(Integer.class, Integer::parseInt);
		add(Long.class, Long::parseLong);
		add(Float.class, Float::parseFloat);
		add(Double.class, Double::parseDouble);
	}

	public void register(ConfigConverter<?> converter) {
		final Class<? extends ConfigConverter> type = converter.getClass();

		this.cached.compute(getTypeParameter(type, CV_TYPE), (t, s) -> {
			return createInfo(s, converter);
		});
	}

	@Override
	public <T> ConfigConverter<T> get(Type type) {
		return (ConfigConverter<T>) this.cached
				.computeIfAbsent(type, t -> createInfo(null, inferConverter(t)))
				.first()
				.instance();
	}

	private <T> void add(Type type, Function<String, T> func) {
		final ConfigConverter<T> conv = node -> node.getValue().map(func);

		this.cached.put(type, createInfo(null, conv));
	}

	private SortedSet<ConverterInfo<?>> createInfo(SortedSet<ConverterInfo<?>> set, ConfigConverter<?> cvt) {
		if (set == null) {
			set = new TreeSet<>();
		}

		final int prio = ofNullable(cvt.getClass().getAnnotation(Priority.class))
				.map(Priority::value)
				.orElse(Integer.MAX_VALUE);

		set.add(new ConverterInfo<>(cvt, prio));

		return set;
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

		throw new ConfigException(format("NO WAY to construct a %s", type.getTypeName()));
	}

	private <T> ConfigConverter<T> parameterizedTypeConverter(ParameterizedType type) {
		final Type rawType = type.getRawType();

		if (!(rawType instanceof Class)) {
			return null;
		}

		final Class<?> rawClass = (Class<?>) rawType;

		if (!rawClass.isInterface()) {
			return null;
		}

		if (Collection.class.isAssignableFrom(rawClass)) {
			final Type actual = type.getActualTypeArguments()[0];
			final ConfigConverter<?> conv = get(actual);
			Supplier<? extends Collection<?>> sup = null;

			if (SortedSet.class == rawClass) {
				sup = TreeSet::new;
			} else if (Set.class == rawClass) {
				sup = HashSet::new;
			} else {
				sup = LinkedList::new;
			}

			return new CollectionConverter(sup, actual, conv);
		}
		if (Map.class.isAssignableFrom(rawClass)) {
			final Type actual = type.getActualTypeArguments()[1];
			final ConfigConverter<?> conv = get(actual);
			Supplier<? extends Map<?, ?>> sup = null;

			if (SortedMap.class == rawClass) {
				sup = TreeMap::new;
			} else {
				sup = LinkedHashMap::new;
			}

			return new MapConverter(sup, actual, conv);
		}

		return null;
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
