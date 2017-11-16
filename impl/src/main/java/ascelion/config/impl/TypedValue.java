
package ascelion.config.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;

public final class TypedValue
{

	private final Converters cvs = new Converters();

	final ConfigNode root;

	public TypedValue( ConfigNode root )
	{
		this.root = root;
	}

	public <T> T getValue( Type type, String prop, int unwrap )
	{
		if( type instanceof ParameterizedType ) {
			final ParameterizedType pt = (ParameterizedType) type;
			final Type raw = pt.getRawType();

			if( raw.equals( Map.class ) ) {
				return (T) getMap( pt.getActualTypeArguments()[0], prop, unwrap );
			}
		}

		return (T) this.cvs.create( type, this.root.getNode( prop ).getValue() );
	}

	private <T> Map<String, T> getMap( Type type, String prop, int unwrap )
	{
		ConfigNode node = this.root.getNode( prop );

		try {
			final String v = node.getValue();

			if( v != null ) {
				node = this.root.getNode( v );
			}
		}
		catch( final ConfigNotFoundException e ) {
			;
		}

		final Map<String, T> m = new TreeMap<>();

		node.asMap()
			.forEach( ( k, v ) -> m.put( k, (T) this.cvs.create( type, v ) ) );

		return m;
	}
}
