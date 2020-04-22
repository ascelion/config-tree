package ascelion.config.read;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConfigInputReader;

import java.util.Collection;

public class SystemInputReader implements ConfigInputReader {

	@Override
	public String defaultResource() {
		return "__SYSTEM__";
	}

	@Override
	public Collection<ConfigInput> read() {
		return asList(new EnvironmentInput(), new SystemPropertiesInput());
	}

	@Override
	public Collection<ConfigInput> read(String source) {
		return emptyList();
	}

}
