package ascelion.config.microprofile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConfigInputReader;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

public class MicroprofileInputReader implements ConfigInputReader {
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
