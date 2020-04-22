
package ascelion.config.eval;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ascelion.config.eval.Expression.Lookup;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class ExpressionTest {

	@Test
	public void run00() {
		final Expression exp = new Expression().withLookup(this::mockEval);
		final String val = exp.eval("${a}").getValue();

		assertThat(val, is("<a>"));
	}

	@Test
	public void run01_1() {
		final Expression exp = new Expression().withLookup(this::mockEval);
		final String val = exp.eval("--${a-${b}-c}--").getValue();

		assertThat(val, is("--<a-<b>-c>--"));
	}

	@Test
	public void run01_2() {
		final Expression exp = new Expression().withLookup(this::mockEval);
		final String val = exp.eval("{a-${b-${c-${d}-e}-f}-g}").getValue();

		assertThat(val, is("{a-<b-<c-<d>-e>-f>-g}"));
	}

	@Test
	public void run02() {
		final Expression exp = new Expression().withLookup(this::mockEval);
		final String val = exp.eval("x-${a-null:-b}-y").getValue();

		assertThat(val, is("x-b-y"));
	}

	@Test
	public void run03() {
		final Expression exp = new Expression().withLookup(this::mockEval);
		final String val = exp.eval("x-${a-null:-${b-null:-c}}-y").getValue();

		assertThat(val, is("x-c-y"));
	}

	@Test
	public void run04() {
		final Expression exp = new Expression().withLookup(this::mockEval);
		final String val = exp.eval("{a-${b-${c-null:-x-${y}-z}-d}-e}").getValue();

		assertThat(val, is("{a-<b-x-<y>-z-d>-e}"));
	}

	@Test
	public void run05() {
		final Expression exp = new Expression().withLookup(this::mockEval);
		final String val = exp.eval("$a:b").getValue();

		assertThat(val, is("$a:b"));
	}

	@Test
	public void run07() {
		final Expression exp = new Expression().withLookup(this::mockEval);
		final String val = exp.eval("${a-null:-b:-c}").getValue();

		assertThat(val, is("b:-c"));
	}

	@Test
	public void runLoop() {
		final int[] count = { 0 };
		final Function<String, Lookup> fun = x -> {
			return new Lookup(Optional.of(format("${X%02d}", ++count[0] % 5)));
		};

		final Expression exp = new Expression()
				.withLookup(fun);

		assertThrows(IllegalStateException.class, () -> exp.eval("${X00}"));
	}

	private Expression.Lookup mockEval(String x) {
		final Optional<String> o = Optional.ofNullable(x.startsWith("null-") || x.endsWith("-null") ? null : "<" + x + ">");

		return new Lookup(o);
	}
}
