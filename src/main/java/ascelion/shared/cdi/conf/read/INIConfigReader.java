
package ascelion.shared.cdi.conf.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;

import static ascelion.shared.cdi.conf.ConfigNode.path;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

@ConfigSource.Type( value = "INI" )
@ApplicationScoped
class INIConfigReader implements ConfigReader
{

	@Override
	public void readConfiguration( ConfigNode root, InputStream is ) throws IOException
	{
		final Ini ini = new Ini( is );

		ini.forEach( ( k, v ) -> {
			add( root, "", k, v );
		} );
	}

	private void add( ConfigNode root, String prefix, String name, Section section )
	{
		final String pfx = path( prefix, name );

		section.forEach( ( k, v ) -> {
			if( ".".equals( pfx ) ) {
				root.set( k, v );
			}
			else {
				root.set( path( pfx, k ), v );
			}
		} );

		Stream.of( section.childrenNames() ).forEach( c -> {
			add( root, pfx, c, section.getChild( c ) );
		} );
	}

}
