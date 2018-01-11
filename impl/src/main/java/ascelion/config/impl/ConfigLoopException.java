
package ascelion.config.impl;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Stream;

import ascelion.config.api.ConfigException;

import static java.util.stream.Collectors.joining;

public final class ConfigLoopException extends ConfigException
{

	static private final ThreadLocal<Deque<String>> DEFINITIONS = new ThreadLocal<Deque<String>>()
	{

		@Override
		protected Deque<String> initialValue()
		{
			return new LinkedList<>();
		};
	};

	static void push( String def )
	{
		final Deque<String> defs = DEFINITIONS.get();

		if( defs.contains( def ) ) {
			throw new ConfigLoopException( defs, def );
		}

		DEFINITIONS.get().addLast( def );
	}

	static void pop()
	{
		DEFINITIONS.get().removeLast();
	}

	ConfigLoopException( String message )
	{
		super( message );
	}

	ConfigLoopException( Collection<String> defs, String content )
	{
		super( Stream.concat( defs.stream(), Stream.of( content ) ).collect( joining( " -> ", "recursive definition: ", "" ) ) );
	}
}
