
package ascelion.config.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

import static java.util.Arrays.asList;

public final class Iterables<T>
{

	@SuppressWarnings( "rawtypes" )
	static private final Predicate ACCEPT = t -> true;

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

	public Iterable<T> get()
	{
		return () -> this.objects.stream().filter( this.filter ).iterator();
	}
}
