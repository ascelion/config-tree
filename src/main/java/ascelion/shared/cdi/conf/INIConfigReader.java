
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

@ConfigSource.Type( value = "INI" )
@ApplicationScoped
class INIConfigReader extends ConfigStore implements ConfigReader
{

	@Override
	public Map<String, Object> readConfiguration( InputStream is ) throws IOException
	{
		final Ini ini = new Ini( is );

		ini.forEach( ( k0, v0 ) -> {
			add( "", k0, v0 );
		} );

		return get();
	}

	private void add( String prefix, String name, Section section )
	{
		final String pfx = isBlank( prefix ) ? name : format( "%s.%s", prefix, name );

		section.forEach( ( k, v ) -> {
			if( ".".equals( pfx ) ) {
				setValue( k, v );
			}
			else {
				setValue( format( "%s.%s", pfx, k ), v );
			}
		} );

		Stream.of( section.childrenNames() ).forEach( c -> {
			add( pfx, c, section.getChild( c ) );
		} );
	}

}
