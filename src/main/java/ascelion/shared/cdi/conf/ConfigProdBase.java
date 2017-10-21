
package ascelion.shared.cdi.conf;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

public abstract class ConfigProdBase
{

	@Inject
	ConfigCollect cc;

	protected final ConfigNode getConfigNode( InjectionPoint ip )
	{
		final ConfigValue ano = getValueAnnotation( ip );
		final String[] vec = ano.value().split( ":" );
		final String path = ConfigNode.path( getPrefix( ip ), vec[0] );
		ConfigNode cn = this.cc.getRoot().getNode( path );

		if( cn == null && vec.length > 1 ) {
			cn = vec[1] != null ? new ConfigNode( path ).set( vec[1] ) : null;
		}
		if( cn == null ) {
			return null;
		}

		return cn;
	}

	protected final String getConfigItem( InjectionPoint ip )
	{
		final ConfigValue ano = getValueAnnotation( ip );
		final String[] vec = ano.value().split( ":" );
		final String path = ConfigNode.path( getPrefix( ip ), vec[0] );
		String cn = this.cc.getRoot().getItem( path );

		if( cn == null && vec.length > 1 ) {
			cn = vec[1];
		}

		return expandValue( cn );
	}

	protected final String expandValue( String value )
	{
		return new Expander( value, x -> {
			return this.cc.getRoot().getItem( x );
		} ).expand();
	}

	protected final String[] expandValues( String value )
	{
		value = new Expander( value, x -> {
			return this.cc.getRoot().getItem( x );
		} ).expand();

		return isNotBlank( value ) ? value.split( "\\s*[;,]\\s*" ) : new String[0];
	}

	final ConfigValue getValueAnnotation( InjectionPoint ip )
	{
		return ip.getAnnotated().getAnnotation( ConfigValue.class );
	}

	private String getPrefix( InjectionPoint ip )
	{
		final ConfigPrefix a = ip.getBean().getBeanClass().getAnnotation( ConfigPrefix.class );

		return a != null ? trimToNull( a.value() ) : null;
	}

}
