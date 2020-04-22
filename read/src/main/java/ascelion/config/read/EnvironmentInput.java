
package ascelion.config.read;

import static java.security.AccessController.doPrivileged;

import ascelion.config.api.ConfigProvider.Builder;
import ascelion.config.spi.ConfigInput;

import java.security.PrivilegedAction;
import java.util.Map;

final class EnvironmentInput extends ConfigInput
{

	@Override
	public int priority()
	{
		return 300;
	}

	@Override
	public void update( Builder bld )
	{
		environment().forEach( ( k, v ) -> {
			bld.set( k, v );

			if( k.indexOf( '_' ) >= 0 ) {
				k = k.replace( '_', '.' )
					.replaceAll( "\\.+", "." )
					.replaceAll( "^\\.|\\.$", "" )
					.toLowerCase();

				if( k.length() > 0 ) {
					bld.set( k, v );
				}
			}
		} );

		bld.set( environment() );
	}

	private Map<String, String> environment()
	{
		return doPrivileged( (PrivilegedAction<Map<String, String>>) () -> System.getenv() );
	}
}
