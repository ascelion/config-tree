package ascelion.config.read;

import java.io.IOException;
import java.net.URL;

import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConfigInputReader;

@ConfigInputReader.Type(value = "YML", suffixes = { "yml", "yaml" })
public class YamlInputReader extends ResourceInputReader {

	@Override
	protected ConfigInput read(URL source) throws IOException {
		return new YamlInput(source);
	}
}
