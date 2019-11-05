package ascelion.config.core;

import ascelion.config.spi.ConfigInputReader;

import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractTest {

	@BeforeEach
	public final void reset() {
		ConfigProviderImpl.reset();
		System.setProperty(ConfigInputReader.RESOURCE_PROP, getClass().getSimpleName());
	}
}
