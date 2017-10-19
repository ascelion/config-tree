
package ascelion.shared.cdi.conf.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import ascelion.shared.cdi.conf.ConfigItem;
import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;
import ascelion.shared.cdi.conf.ConfigStore;

import static ascelion.shared.cdi.conf.ConfigItem.fullPath;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

@ConfigSource.Type( value = "INI" )
@ApplicationScoped
class INIConfigReader extends ConfigStore implements ConfigReader
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
		final String pfx = fullPath( prefix, name );

		section.forEach( ( k, v ) -> {
			if( ".".equals( pfx ) ) {
				root.set( k, v );
			}
			else {
				root.set( fullPath( pfx, k ), v );
			}
		} );

		Stream.of( section.childrenNames() ).forEach( c -> {
			add( root, pfx, c, section.getChild( c ) );
		} );
	}

	@Override
	public Map<String, ? extends ConfigItem> readConfiguration( InputStream is ) throws IOException
	{
		reset();

		final Ini ini = new Ini( is );

		ini.forEach( ( k0, v0 ) -> {
			add( "", k0, v0 );
		} );

		return get();
	}

	private void add( String prefix, String name, Section section )
	{
		final String pfx = fullPath( prefix, name );

		section.forEach( ( k, v ) -> {
			if( ".".equals( pfx ) ) {
				setValue( k, v );
			}
			else {
				setValue( fullPath( pfx, k ), v );
			}
		} );

		Stream.of( section.childrenNames() ).forEach( c -> {
			add( pfx, c, section.getChild( c ) );
		} );
	}

}
