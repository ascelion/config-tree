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

	@SuppressWarnings("rawtypes")
	public void register(ConfigConverter<?> converter) {
		final Class<? extends ConfigConverter> type = converter.getClass();

		this.cached.compute(getTypeParameter(type, CV_TYPE), (t, s) -> {
			return createInfo(s, converter);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ConfigConverter<T> get(Type type) {
		return (ConfigConverter<T>) this.cached
				.computeIfAbsent(type, t -> createInfo(null, inferConverter(t)))
				.first()
				.instance();
	}

	private <T> void add(Type type, Function<String, T> func) {
		final ConfigConverter<T> cv = node -> node.getValue().map(func);

		this.cached.put(type, createInfo(null, cv));
	}

	private SortedSet<ConverterInfo<?>> createInfo(SortedSet<ConverterInfo<?>> set, ConfigConverter<?> cvt) {
		if (set == null) {
			set = new TreeSet<>();
		}

		final int p = ofNullable(cvt.getClass().getAnnotation(Priority.class))
				.map(Priority::value)
				.orElse(Integer.MAX_VALUE);

		set.add(new ConverterInfo<>(cvt, p));

		return set;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> ConfigConverter<T> inferConverter(Type type) {
		if (type instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) type;
			final Type rt = pt.getRawType();

			if (rt instanceof Class) {
				final Class<?> rc = (Class<?>) rt;

				if (rc.isInterface()) {
					if (Collection.class.isAssignableFrom(rc)) {
						final Type it = pt.getActualTypeArguments()[0];
						final ConfigConverter<?> cv = get(it);
						Supplier<? extends Collection<?>> sup = null;

						if (SortedSet.class == rc) {
							sup = TreeSet::new;
						} else if (Set.class == rc) {
							sup = HashSet::new;
						} else {
							sup = LinkedList::new;
						}

						return new CollectionConverter(sup, it, cv);
					}
					if (Map.class.isAssignableFrom(rc)) {
						final Type it = pt.getActualTypeArguments()[1];
						final ConfigConverter<?> cv = get(it);
						Supplier<? extends Map<?, ?>> sup = null;

						if (SortedMap.class == rc) {
							sup = TreeMap::new;
						} else {
							sup = LinkedHashMap::new;
						}

						return new MapConverter(sup, it, cv);
					}
				}
			}
		}
		if (type instanceof GenericArrayType) {
			final GenericArrayType at = (GenericArrayType) type;
			final Type ct = at.getGenericComponentType();
			final ConfigConverter<T> cv = get(ct);

			return new ArrayConverter(ct, cv);
		}
		if (type instanceof Class) {
			final Class<T> cls = (Class<T>) type;

			if (cls.isEnum()) {
				return new EnumConverter(cls);
			}
			if (cls.isArray()) {
				final Class<?> ct = cls.getComponentType();

				if (ct.isPrimitive()) {
					return new PrimitiveArrayConverter(ct, get(ct));
				} else {
					return new ArrayConverter(ct, get(ct));
				}
			}
//			if (cls.isInterface()) {
//				return new InterfaceConverter<>(cls);
//			}

			final ConfigConverter<T> fc = fromClass(cls);

			if (fc != null) {
				return fc;
			}
		}

		throw new ConfigException(format("NO WAY to construct a %s", type.getTypeName()));
	}

	private <T> ConfigConverter<T> fromClass(Class<T> cls) {
		ConfigConverter<T> c;

		if ((c = fromConstructor(cls, String.class)) != null) {
			return c;
		}
		if ((c = fromConstructor(cls, CharSequence.class)) != null) {
			return c;
		}

		for (final String name : CREATE_METHODS) {
			if ((c = fromMethod(cls, name, String.class)) != null) {
				return c;
			}
			if ((c = fromMethod(cls, name, CharSequence.class)) != null) {
				return c;
			}
		}

		return null;
	}

	private <T> ConfigConverter<T> fromConstructor(Class<T> cls, Class<? extends CharSequence> paramType) {
		try {
			final Constructor<T> c = cls.getDeclaredConstructor(paramType);

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
		} catch (final NoSuchMethodException e) {
			return null;
		}
	}

	private <T> ConfigConverter<T> fromMethod(Class<T> cls, String name, Class<? extends CharSequence> paramType) {
		try {
			final Method m = cls.getDeclaredMethod(name, paramType);

			if (!Modifier.isStatic(m.getModifiers())) {
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

					m.setAccessible(true);

					return Optional.of((T) m.invoke(null, value));
				}
			};
		} catch (final NoSuchMethodException e) {
			return null;
		}
	}
}
