
package ascelion.config.eclipse.ext;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ascelion.config.api.ConvertersRegistry;
import ascelion.config.utils.Expression;

import static java.lang.String.format;

import org.eclipse.microprofile.config.spi.ConfigSource;

public abstract class AbstractConfig implements ConfigExt
{

	static private final Logger L = Logger.getLogger( org.eclipse.microprofile.config.Config.class.getName() );

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
			if( L.isLoggable( Level.FINEST ) ) {
				L.finest( format( "Checking %s in %s", key, cs ) );
			}

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
