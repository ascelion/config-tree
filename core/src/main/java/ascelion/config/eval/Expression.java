package ascelion.config.eval;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Optional;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public final class Expression {
	static final String PREFIX_DEF = "${";
	static final String SUFFIX_DEF = "}";
	static final String VALUE_DEF = ":-";

	@NonNull
	Function<String, Lookup> lookup = x -> new Lookup(Optional.of(x));
	@NonNull
	char[] varPrefix = PREFIX_DEF.toCharArray();
	@NonNull
	char[] valueSep = VALUE_DEF.toCharArray();
	@NonNull
	char[] varSuffix = SUFFIX_DEF.toCharArray();

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	static public class Lookup {
		private final Optional<String> value;
		@Getter
		private final boolean undefined;

		public Lookup() {
			this(null, true);
		}

		public Lookup(Optional<String> value) {
			this(value, false);
		}

		public String getValue(String def) {
			if (this.undefined) {
				return trimToNull(def);
			}

			return trimToEmpty(this.value.orElse(def));
		}
	}

	@RequiredArgsConstructor
	@Getter
	static public final class Result {
		private final String expression;
		private final String value;
		private final String lastVariable;

		public boolean isResolved() {
			return !this.expression.equals(this.value);
		}

		public boolean isEmpty() {
			return this.value == null;
		}
	}

	public Result eval(String expression) {
		final Replacer rep = new Replacer(this);
		final Buffer buf = rep.replace(expression);
		final String val = buf.toString().trim();

		return new Result(expression, val.isEmpty() ? null : val, rep.getLastVariable());
	}

	public Expression withLookup(@NonNull Function<String, Lookup> lookup) {
		this.lookup = lookup;

		return this;
	}

	public Expression withPrefix(String expPrefix) {
		this.varPrefix = expPrefix.toCharArray();

		return this;
	}

	public Expression withValueSep(String expDefault) {
		this.valueSep = expDefault.toCharArray();

		return this;
	}

	public Expression withSuffix(String expSuffix) {
		this.varSuffix = expSuffix.toCharArray();

		return this;
	}
}
