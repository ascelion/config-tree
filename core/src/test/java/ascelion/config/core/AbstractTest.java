
package ascelion.config.core;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;
import ascelion.config.spi.ConfigInputReader;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;

@RequiredArgsConstructor
public abstract class AbstractTest
{

	private final String resource;

	protected ConfigRoot root;

	public AbstractTest()
	{
		this.resource = getClass().getSimpleName();
	}

	@BeforeEach
	public final void reset()
	{
		ConfigProviderImpl.reset();

		System.setProperty( ConfigInputReader.RESOURCE_PROP, this.resource );

		this.root = ConfigProvider.root();
	}
}
