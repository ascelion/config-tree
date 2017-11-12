
package ascelion.config.impl;

import java.util.Comparator;

import ascelion.config.api.ConfigSource;

final class ConfigSourceCMP implements Comparator<ConfigSource>
{

	static final Comparator<ConfigSource> INSTANCE = new ConfigSourceCMP();

	private ConfigSourceCMP()
	{
	}

	@Override
	public int compare( ConfigSource o1, ConfigSource o2 )
	{
		if( o1 == o2 ) {
			return 0;
		}
		if( o1 == null || o2 == null ) {
			return o1 == null ? -1 : +1;
		}

		int c;

		if( ( c = Integer.compare( o1.priority(), o2.priority() ) ) != 0 ) {
			return c;
		}

		if( ( c = o1.type().compareTo( o2.type() ) ) != 0 ) {
			return c;
		}

		if( ( c = o1.value().compareTo( o2.value() ) ) != 0 ) {
			return c;
		}

		return 0;
	}

}
