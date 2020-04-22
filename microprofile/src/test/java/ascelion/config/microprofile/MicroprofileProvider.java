
package ascelion.config.microprofile;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

public class MicroprofileProvider implements TestTemplateInvocationContextProvider
{

	@Override
	public boolean supportsTestTemplate( ExtensionContext context )
	{
		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts( ExtensionContext context )
	{
		return Stream.of( io.helidon.microprofile.config.MpConfigProviderResolver.class,
			io.smallrye.config.SmallRyeConfigProviderResolver.class,
			org.apache.geronimo.config.DefaultConfigProvider.class )
			.map( t -> createInvocationContext( context, t ) );
	}

	private TestTemplateInvocationContext createInvocationContext( ExtensionContext context, Class<? extends ConfigProviderResolver> type )
	{
		return new TestTemplateInvocationContext()
		{

			@Override
			public String getDisplayName( int invocationIndex )
			{
				return type.getName();
			}

			@Override
			public List<Extension> getAdditionalExtensions()
			{
				return singletonList( new ParameterResolver()
				{

					@Override
					public boolean supportsParameter( ParameterContext parameterContext, ExtensionContext extensionContext ) throws ParameterResolutionException
					{
						return ConfigProviderResolver.class.isAssignableFrom( parameterContext.getParameter().getType() );
					}

					@Override
					public Object resolveParameter( ParameterContext parameterContext, ExtensionContext extensionContext ) throws ParameterResolutionException
					{
						try {
							return type.newInstance();
						}
						catch( InstantiationException | IllegalAccessException e ) {
							throw new ParameterResolutionException( type.getName(), e );
						}
					}
				} );
			}
		};
	}

}
