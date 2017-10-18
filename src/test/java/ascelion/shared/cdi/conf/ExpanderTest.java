
package ascelion.shared.cdi.conf;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ExpanderTest
{

	@Test( expected = IllegalArgumentException.class )
	public void runLoop()
	{
		final Map<String, String> options = new HashMap<>();

		options.put( "root", "1-${value1}-${value2}-2" );
		options.put( "value1", "${value}-1" );
		options.put( "value2", "${value}-2" );
		options.put( "value", "${root}" );

		final Expander exp = new Expander( "${root}", options::get );

		exp.expand();
	}

	@Test
	public void run1()
	{
		final Map<String, String> options = new HashMap<>();

		options.put( "root", "1-${value1}-${value2}-2" );
		options.put( "value1", "${val1}-1" );
		options.put( "value2", "${val2}-2" );
		options.put( "val1", "v" );
		options.put( "val2", "valval(${val})" );

		final Expander exp = new Expander( "${root}", options::get );
		final String root = exp.expand();

		assertThat( root, is( "1-v-1-valval(${val})-2-2" ) );
	}

	@Test
	public void run2()
	{
		final Map<String, String> options = new HashMap<>();

		options.put( "p1", "1" );
		options.put( "p2", "2" );
		options.put( "p3", "3" );
		options.put( "pv", "[${p1}, ${p2}, ${p3}]" );

		final Expander exp = new Expander( "${pv}", options::get );
		final String root = exp.expand();

		assertThat( root, is( "[1, 2, 3]" ) );
	}

	@Test
	public void run3()
	{
		final Map<String, String> options = new HashMap<>();

		options.put( "p1", "100" );
		options.put( "p2", "200" );
		options.put( "p3", "300" );
		options.put( "pv", "[${p1}, ${p2}, ${p3}]" );

		final Expander exp = new Expander( "${pv}", options::get );
		final String root = exp.expand();

		assertThat( root, is( "[100, 200, 300]" ) );
	}

	@Test
	public void run4()
	{
		final Map<String, String> options = new HashMap<>();

		options.put( "A", "1" );
		options.put( "B", "2" );
		options.put( "C", "3" );
		options.put( "D", "${A}-${B}-${C}" );

		final Expander exp = new Expander( "${D}", options::get );
		final String root = exp.expand();

		assertThat( root, is( "1-2-3" ) );
	}

}
