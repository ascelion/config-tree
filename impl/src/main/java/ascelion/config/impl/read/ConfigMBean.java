
package ascelion.config.impl.read;

public interface ConfigMBean
{

	String getName();

	String getPath();

	String getExpandedValue();

	String getValue();

	void setValue( String value );
}
