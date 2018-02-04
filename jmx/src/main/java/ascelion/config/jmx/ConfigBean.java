
package ascelion.config.jmx;

import javax.management.MXBean;

@MXBean
public interface ConfigBean
{

	String getPath();

	String getExpression();

	String getValue();

	String getDefaultValue();
}
