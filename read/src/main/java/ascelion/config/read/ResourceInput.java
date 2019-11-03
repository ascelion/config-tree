package ascelion.config.read;

import java.net.URL;

import ascelion.config.spi.ConfigInput;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class ResourceInput implements ConfigInput {
	private final URL source;

	@Override
	public String name() {
		return this.source.toExternalForm();
	}

	@Override
	public String toString() {
		return name();
	}
}
