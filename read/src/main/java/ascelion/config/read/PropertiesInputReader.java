
package ascelion.config.read;

import static java.util.Collections.singleton;

import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConfigInputReader;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

@ConfigInputReader.Type( value = "PRP", suffixes = "properties" )
public class PropertiesInputReader extends ResourceInputReader
{

	@Override
	protected Collection<ConfigInput> readFrom( URL source ) throws IOException
	{
		return singleton( new PropertiesInput( source ) );
	}
}
