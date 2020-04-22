
package ascelion.config.cdi;

import ascelion.cdi.metadata.AnnotatedTypeModifier;
import ascelion.config.api.ConfigPrefix;
import ascelion.config.api.ConfigRoot;
import ascelion.config.api.ConfigValue;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConverterFactory;

import java.lang.reflect.Type;
import java.util.Optional;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Unmanaged;

public class BeanConverterFactory implements ConverterFactory
{

	@Override
	public <T> ConfigConverter<T> get( Type type )
	{
		if( !( type instanceof Class<?> ) ) {
			return null;
		}

		final Class<T> theType = (Class<T>) type;

		if( !theType.isAnnotationPresent( ConfigValue.class ) ) {
			return null;
		}

		return node -> buildType( theType, node.getPath() );
	}

	protected <T> Optional<T> buildType( Class<T> type, String path )
	{
		final BeanManager bm = CDI.current().getBeanManager();
		final AnnotatedTypeModifier<T> tmod = AnnotatedTypeModifier.create( bm.createAnnotatedType( type ) );

		tmod.type().add( new ConfigPrefix.Literal( path ) );

		final T instance = new Unmanaged<>( bm, type )
			.newInstance()
			.produce()
			.inject()
			.postConstruct()
			.get();

		final ConfigProcessor<T> proc = new ConfigProcessor<>( tmod.get() );

		if( proc.values().size() > 0 ) {
			final ConfigRoot root = CDI.current().select( ConfigRoot.class ).get();

			proc.fields().forEach( f -> ConfigInjectionTarget.inject( root, instance, f ) );
			proc.methods().forEach( m -> ConfigInjectionTarget.inject( root, instance, m ) );
		}

		return Optional.of( instance );
	}
}
