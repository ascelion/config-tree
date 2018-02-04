
package ascelion.config.jmx;

import javax.management.MXBean;

@MXBean
public interface WritableConfigBean extends ConfigBean
{

	void setExpression( String value );

}
