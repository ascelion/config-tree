package ascelion.config.microprofile;

import static java.lang.String.format;

import ascelion.config.api.ConfigProvider.Builder;
import ascelion.config.spi.ConfigInput;

import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.spi.ConfigSource;

@RequiredArgsConstructor
final class MicroprofileInput extends ConfigInput {
	static private final Logger LOG = Logger.getLogger(MicroprofileInput.class.getName());
	private final ConfigSource source;

	@Override
	public String name() {
		return "[MP] " + this.source.getName();
	}

	@Override
	public int priority() {
		return this.source.getOrdinal();
	}

	@Override
	public void update(Builder bld) {
		LOG.finest(format("Updating builder from %s", name()));

		bld.set(this.source.getProperties());
	}

}
