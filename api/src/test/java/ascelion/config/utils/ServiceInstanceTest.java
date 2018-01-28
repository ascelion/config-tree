
package ascelion.config.utils;

import ascelion.config.utils.impl.Interface;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ServiceInstanceTest
{

	@Test
	public void byFactory()
	{
		final Interface impl = new ServiceInstance<>( Interface.class ).get();

		assertNotNull( impl );
		assertNotNull( impl.getClassLoader() );
	}

}
