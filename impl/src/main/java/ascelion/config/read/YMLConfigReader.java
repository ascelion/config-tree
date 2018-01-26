
package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigReader;

import static ascelion.config.conv.Utils.path;

import org.yaml.snakeyaml.Yaml;

@ConfigReader.Type( value = YMLConfigReader.TYPE, types = { "YAML" } )
public class YMLConfigReader extends ResourceReader
{

	static public final String TYPE = "YML";

	@Override
	protected Map<String, String> readConfiguration( InputStream is ) throws IOException
	{
		final Map<String, String> map = new TreeMap<>();
		final Yaml yml = new Yaml();

		yml.loadAll( is )
			.forEach( o -> {
				add( "", map, o );
			} );

		return map;
	}

	private void add( String path, Map<String, String> target, Object value )
	{
		if( value instanceof Map ) {
			final Map<String, Object> ms = (Map<String, Object>) value;

			ms.forEach( ( k, s ) -> {
				add( path( path, k ), target, s );
			} );

			return;
		}
		if( value instanceof Collection ) {
			final Collection<?> c = (Collection<?>) value;

			add( path, target, c.stream().map( Object::toString ).collect( Collectors.joining( "," ) ) );

			return;
		}
		if( value instanceof Object[] ) {
			final Object[] v = (Object[]) value;

			add( path, target, Stream.of( v ).map( Object::toString ).collect( Collectors.joining( "," ) ) );

			return;
		}

		target.put( path, Objects.toString( value, null ) );
	}
}
