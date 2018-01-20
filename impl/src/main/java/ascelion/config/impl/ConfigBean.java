
package ascelion.config.impl;

import javax.management.MXBean;

@MXBean
public interface ConfigBean
{

	String getName();

	String getPath();

	String getValue();

	String getRawValue();

	void setValue( String value );
}
