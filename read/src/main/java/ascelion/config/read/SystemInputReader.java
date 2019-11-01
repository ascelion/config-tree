package ascelion.config.read;

import java.io.IOException;
import java.util.Collection;

import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConfigInputReader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class SystemInputReader implements ConfigInputReader {

	@Override
	public String defaultResource() {
		return "__SYSTEM__";
	}

	@Override
	public Collection<ConfigInput> read() throws IOException {
		return asList(/* new EnvironmentInput(), */new SystemPropertiesInput());
	}

	@Override
	public Collection<ConfigInput> read(String source) throws IOException {
		return emptyList();
	}

}
