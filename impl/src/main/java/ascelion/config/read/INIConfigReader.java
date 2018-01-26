
package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import ascelion.config.api.ConfigReader;

import static ascelion.config.conv.Utils.path;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

@ConfigReader.Type( value = INIConfigReader.TYPE )
public class INIConfigReader extends ResourceReader
{

	static public final String TYPE = "INI";

	@Override
	protected Map<String, String> readConfiguration( final InputStream is ) throws IOException
	{
		final Map<String, String> map = new TreeMap<>();
		final Ini ini = new Ini( is );

		ini.forEach( ( k, v ) -> {
			add( map, "", k, v );
		} );

		return map;
	}

	static void add( Map<String, String> map, String prefix, String name, Section section )
	{
		final String pfx = path( prefix, name );

		section.forEach( ( k, v ) -> {
			if( ".".equals( pfx ) ) {
				map.put( k, v );
			}
			else {
				map.put( path( pfx, k ), v );
			}
		} );

		Stream.of( section.childrenNames() ).forEach( c -> {
			add( map, pfx, c, section.getChild( c ) );
		} );
	}

}
