
package ascelion.config.impl;

import javax.management.MXBean;

@MXBean
public interface ConfigBean
{

	String getName();

	String getPath();

	String getExpression();

	void setExpression( String value );

	String getValue();
}
