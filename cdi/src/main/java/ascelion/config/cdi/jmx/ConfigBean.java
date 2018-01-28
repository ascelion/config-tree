
package ascelion.config.cdi.jmx;

import javax.management.MXBean;

@MXBean
public interface ConfigBean
{

	String getPath();

	String getExpression();

	void setExpression( String value );

	String getValue();

	String getDefaultValue();
}
