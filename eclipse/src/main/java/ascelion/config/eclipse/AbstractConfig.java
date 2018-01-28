
package ascelion.config.eclipse;

import java.lang.reflect.Type;

import ascelion.config.api.ConvertersRegistry;
import ascelion.config.eclipse.ext.ConfigExt;
import ascelion.config.utils.Expression;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.eclipse.microprofile.config.spi.ConfigSource;

public abstract class AbstractConfig implements ConfigExt
{

	private final ConvertersRegistry cvs;

	protected AbstractConfig( ConvertersRegistry cvs )
	{
		this.cvs = cvs;
	}

	@Override
	public final String getValue( String propertyName )
	{
		for( final ConfigSource cs : getConfigSources() ) {
			final String val = cs.getValue( propertyName );

			if( val != null ) {
				return val;
			}
		}

		return null;
	}

	@Override
	public final String getValue( String propertyName, boolean evaluate )
	{
		String val = getValue( propertyName );

		if( evaluate && isNotBlank( val ) ) {
			final Expression exp = new Expression( this::lookup, val );

			val = exp.getValue();
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

	private Expression.Result lookup( String key )
	{
		final String val = getValue( key, false );

		return val != null ? new Expression.Result( val ) : new Expression.Result();
	}
}
