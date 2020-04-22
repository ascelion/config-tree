
package ascelion.config.convert;

import static java.util.Optional.ofNullable;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Priority;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

final class PrioritizedCollection<T> extends AbstractCollection<T>
{

	@RequiredArgsConstructor( access = AccessLevel.PACKAGE )
	static class Holder<T> implements Comparable<Holder<T>>
	{

		final T instance;
		final int priority;

		Holder( T instance )
		{
			this.instance = instance;
			this.priority = ofNullable( instance.getClass().getAnnotation( Priority.class ) )
				.map( Priority::value )
				.orElse( 0 );
		}

		@Override
		public int compareTo( Holder<T> that )
		{
			return Integer.compare( this.priority, that.priority );
		}
	}

	private final List<Holder<T>> delegate = new ArrayList<>();

	@Override
	public Iterator<T> iterator()
	{
		final Iterator<Holder<T>> delegate = this.delegate.iterator();

		return new Iterator<T>()
		{

			@Override
			public boolean hasNext()
			{
				return delegate.hasNext();
			}

			@Override
			public T next()
			{
				return delegate.next().instance;
			}
		};
	}

	@Override
	public int size()
	{
		return this.delegate.size();
	}

	public boolean add( T e, int p )
	{
		this.delegate.add( new Holder<>( e, p ) );

		Collections.sort( this.delegate );

		return true;
	}

	@Override
	public boolean add( T e )
	{
		this.delegate.add( new Holder<>( e ) );

		Collections.sort( this.delegate );

		return true;
	}

	public T head()
	{
		if( isEmpty() ) {
			throw new NoSuchElementException();
		}

		return this.delegate.get( 0 ).instance;
	}
}
