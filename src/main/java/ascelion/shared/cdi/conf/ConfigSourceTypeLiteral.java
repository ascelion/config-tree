
package ascelion.shared.cdi.conf;

import javax.enterprise.util.AnnotationLiteral;

class ConfigSourceTypeLiteral extends AnnotationLiteral<ConfigSource.Type> implements ConfigSource.Type
{

	private final String value;
	private final String[] types;

	ConfigSourceTypeLiteral( String value, String... types )
	{
		this.value = value;
		this.types = types;
	}

	@Override
	public String value()
	{
		return this.value;
	}

	@Override
	public String[] types()
	{
		return this.types;
	}

}
