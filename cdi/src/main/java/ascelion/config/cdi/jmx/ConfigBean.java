
package ascelion.config.cdi.jmx;

import javax.management.MXBean;

@MXBean
public interface ConfigBean
{

	String getPath();

	String getValue();

	void setValue( String value );
}
