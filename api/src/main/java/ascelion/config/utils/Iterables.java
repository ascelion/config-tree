
package ascelion.config.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedSet;

public final class Iterables<T>
{

	@SuppressWarnings( "rawtypes" )
	static private final Predicate ACCEPT = t -> true;

	private final LazyValue<Iterable<T>> reference = new LazyValue<>();
	private final Collection<T> objects = synchronizedSet( new HashSet<>() );
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

	public Iterable<T> get( Supplier<Iterable<T>> additional )
	{
		return () -> this.reference.get( () -> build( additional ) ).iterator();
	}

	private Iterable<T> build( Supplier<Iterable<T>> additional )
	{
		final Collection<T> built = new ArrayList<>();

		built.addAll( this.objects );
		additional.get().forEach( built::add );

		return built;
	}
}
