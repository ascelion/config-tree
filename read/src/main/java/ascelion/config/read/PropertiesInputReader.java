package ascelion.config.read;

import java.io.IOException;
import java.net.URL;

import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConfigInputReader;

@ConfigInputReader.Type(value = "PRP", suffixes = "properties")
public class PropertiesInputReader extends ResourceInputReader {

	@Override
	protected ConfigInput read(URL source) throws IOException {
		return new PropertiesInput(source);
	}
}
