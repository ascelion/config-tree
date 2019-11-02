package ascelion.config.eval;

import java.util.Optional;
import java.util.function.Function;

import lombok.NonNull;

public final class Expression {
	static final String PREFIX_DEF = "${";
	static final String SUFFIX_DEF = "}";
	static final String VALUE_DEF = ":-";

	@NonNull
	Function<String, Optional<String>> lookup = x -> Optional.of(x);
	@NonNull
	char[] varPrefix = PREFIX_DEF.toCharArray();
	@NonNull
	char[] valueSep = VALUE_DEF.toCharArray();
	@NonNull
	char[] varSuffix = SUFFIX_DEF.toCharArray();

	public String eval(String expression) {
		return new Replacer(this)
				.replace(new Buffer(expression))
				.toString();
	}

	public Expression withLookup(@NonNull Function<String, Optional<String>> lookup) {
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
