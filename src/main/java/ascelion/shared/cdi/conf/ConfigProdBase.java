
package ascelion.shared.cdi.conf;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class ConfigProdBase
{

	@Inject
	private ConfigCollect cc;

	protected final ConfigValue getAnnotation( InjectionPoint ip )
	{
		return ip.getAnnotated().getAnnotation( ConfigValue.class );
	}

	protected ConfigItem getConfig( InjectionPoint ip )
	{
		final ConfigValue ano = getAnnotation( ip );
		final String[] vec = ano.value().split( ":" );
		ConfigItem ci = this.cc.store().getValue( vec[0] );

		if( ci == null && vec.length > 1 ) {
			ci = vec[1] != null ? new ConfigItemImpl( "" ).set( vec[1] ) : null;
		}
		if( ci == null ) {
			return null;
		}

		return ci;
	}

	protected String getConfigValue( InjectionPoint ip )
	{
		final ConfigValue ano = getAnnotation( ip );
		final String[] vec = ano.value().split( ":" );
		ConfigItem ci = this.cc.store().getValue( vec[0] );

		if( ci == null && vec.length > 1 ) {
			ci = vec[1] != null ? new ConfigItemImpl( vec[1] ) : null;
		}
		if( ci == null ) {
			return null;
		}

		return expandValue( ci.getValue() );
	}

	protected String expandValue( String value )
	{
		return new Expander( value, x -> {
			final ConfigItem ci = this.cc.store().getValue( x );

			return ci != null ? ci.getValue() : null;
		} ).expand();
	}

	protected String[] expandValues( String value )
	{
		value = new Expander( value, x -> {
			final ConfigItem ci = this.cc.store().getValue( x );

			return ci != null ? ci.getValue() : null;
		} ).expand();

		return isNotBlank( value ) ? value.split( "\\s*[;,]\\s*" ) : new String[0];
	}
}
