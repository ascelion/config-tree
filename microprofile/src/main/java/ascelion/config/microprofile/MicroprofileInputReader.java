package ascelion.config.microprofile;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConfigInputReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

public final class MicroprofileInputReader implements ConfigInputReader {
	static private final Logger LOG = Logger.getLogger(MicroprofileInputReader.class.getName());
	static private final Mediator MEDIATOR = new Mediator();

	@Override
	public Set<String> suffixes() {
		return emptySet();
	}

	@Override
	public String defaultResource() {
		return "META-INF/microprofile-config";
	}

	@Override
	public Collection<ConfigInput> read(String source) {
		if (MEDIATOR.acquire()) {
			try {
				final Config config = new InstanceProvider<>(Config.class,
						() -> ConfigProviderResolver.instance().getConfig())
								.get();
				final List<ConfigInput> inputs = new ArrayList<>();

				config.getConfigSources()
						.forEach(src -> {
							if (!(src instanceof ExpressionConfigSource)) {
								LOG.finest(format("Considering config source %s", src.getName()));

								inputs.add(new MicroprofileInput(src));
							}
						});

				return inputs;
			} finally {
				MEDIATOR.release();
			}
		} else {
			return emptyList();
		}
	}
}
