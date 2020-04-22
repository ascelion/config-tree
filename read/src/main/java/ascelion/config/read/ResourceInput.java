
package ascelion.config.read;

import ascelion.config.spi.ConfigInput;

import java.net.URL;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class ResourceInput extends ConfigInput
{

	private final URL source;

	@Override
	public String name()
	{
		return this.source.toExternalForm();
	}
}
