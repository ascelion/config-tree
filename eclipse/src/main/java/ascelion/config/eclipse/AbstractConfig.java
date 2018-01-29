
package ascelion.config.eclipse;

import java.lang.reflect.Type;
import java.util.Map;

import ascelion.config.api.ConvertersRegistry;
import ascelion.config.eclipse.ext.ConfigExt;
import ascelion.config.utils.Expression;

import org.eclipse.microprofile.config.spi.ConfigSource;

public abstract class AbstractConfig implements ConfigExt
{

	private final ConvertersRegistry cvs;

	protected AbstractConfig( ConvertersRegistry cvs )
	{
		this.cvs = cvs;
	}

	@Override
	public final String getValue( String key )
	{
		return readValue( key ).get();
	}

	@Override
	public final Value getValue( String key, boolean evaluate )
	{
		final Value val = readValue( key );

		if( evaluate && !val.undefined() ) {
			final Expression exp = new Expression( this::lookup, val.get() );

			return new Value( exp.getValue() );
		}

		return val;
	}

	@Override
	public final <T> T convert( String value, Type type )
	{
		try {
			return (T) this.cvs.getConverter( type )
				.create( value );
		}
		catch( final IllegalArgumentException e ) {
			throw e;
		}
		catch( final RuntimeException e ) {
			throw new IllegalArgumentException( value, e );
		}
	}

	protected final Value readValue( String key )
	{
		for( final ConfigSource cs : getConfigSources() ) {
			final Map<String, String> map = cs.getProperties();

			if( map.containsKey( key ) ) {
				return new Value( cs.getValue( key ) );
			}
		}

		return new Value();
	}

	private Value lookup( String key )
	{
		return getValue( key, false );
	}
}
