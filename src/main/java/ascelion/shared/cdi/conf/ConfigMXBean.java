
package ascelion.shared.cdi.conf;

public interface ConfigMXBean
{

	String getName();

	String getPath();

	String getValue();

	void setValue( String value );
}
