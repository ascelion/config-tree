
package ascelion.config.microprofile;

import ascelion.config.api.ConfigProvider.Builder;
import ascelion.config.spi.ConfigInput;

import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.spi.ConfigSource;

@RequiredArgsConstructor
final class MicroprofileInput extends ConfigInput
{

	private final ConfigSource source;

	@Override
	public String name()
	{
		return "[MP] " + this.source.getName();
	}

	@Override
	public int priority()
	{
		return this.source.getOrdinal();
	}

	@Override
	public void update( Builder bld )
	{
		bld.set( this.source.getProperties() );
	}

}
