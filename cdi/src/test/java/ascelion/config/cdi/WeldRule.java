
package ascelion.config.cdi;

import static java.util.Arrays.asList;

import ascelion.config.spi.ConfigInputReader;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jboss.weld.junit5.WeldInitiator;

@NoArgsConstructor( access = AccessLevel.NONE )
class WeldRule
{

	static WeldInitiator createWeldRule( Object test, Class<?>... beans )
	{
		System.setProperty( ConfigInputReader.RESOURCE_PROP, test.getClass().getSimpleName() );

		final List<Class<?>> classes = new ArrayList<>( asList( beans ) );

		classes.add( ascelion.config.cdi.ConfigExtension.class );
		//		classes.add(io.smallrye.config.inject.ConfigExtension.class);

		return WeldInitiator
			.from( classes.toArray( new Class[0] ) )
			.inject( test )
			.build();
	}
}
