
package ascelion.config.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.asList;

public final class Iterables<T>
{

	@SuppressWarnings( "rawtypes" )
	static private final Predicate ACCEPT = t -> true;

	private final References<Iterable<T>> references = new References<>();
	private final Collection<T> objects = new HashSet<>();
	private Predicate<T> filter = ACCEPT;

	public void add( T... objects )
	{
		this.objects.addAll( asList( objects ) );
	}

	public void add( Iterable<T> objects )
	{
		objects.forEach( this.objects::add );
	}

	public void filter( Predicate<T> f )
	{
		this.filter = this.filter != null ? this.filter : ACCEPT;
	}

	public Iterable<T> get( ClassLoader cld, Function<ClassLoader, Iterable<T>> additional )
	{
		return () -> this.references.get( cld, x -> build( x, additional ) ).iterator();
	}

	private Iterable<T> build( ClassLoader cld, Function<ClassLoader, Iterable<T>> additional )
	{
		final Collection<T> built = new ArrayList<>();

		built.addAll( this.objects );
		additional.apply( cld ).forEach( built::add );

		return built;
	}
}
