
package ascelion.config.jmx;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import ascelion.cdi.type.AnnotatedTypeW;

import static java.util.Collections.unmodifiableMap;

public class JMXExtension implements Extension
{

	private final Map<String, JMXConfig> jmxConfigs = new TreeMap<>();

	void beforeBeanDiscovery( @Observes BeforeBeanDiscovery event, BeanManager bm )
	{
		addType( JMXReader.class, bm, event );
	}

	<X> void collectWritable( @Observes ProcessAnnotatedType<X> event )
	{
		final AnnotatedType<X> t = event.getAnnotatedType();

		processAnnotated( t );

		t.getConstructors().forEach( this::processAnnotated );
		t.getMethods().forEach( this::processAnnotated );
		t.getFields().forEach( this::processAnnotated );
	}

	Map<String, JMXConfig> jmxConfigs()
	{
		return unmodifiableMap( this.jmxConfigs );
	}

	private void addType( Class<?> type, BeanManager bm, BeforeBeanDiscovery event )
	{
		AnnotatedType<?> at = bm.createAnnotatedType( type );

		at = new AnnotatedTypeW<>( at );

		if( at.getAnnotations().stream().allMatch( a -> !bm.isScope( a.annotationType() ) ) ) {
			at.getAnnotations().add( ApplicationScoped.Literal.INSTANCE );
		}

		event.addAnnotatedType( at, type.getName() );
	}

	private void processAnnotated( Annotated t )
	{
		t.getAnnotations( JMXConfig.class )
			.forEach( a -> Stream.of( a.value() )
				.forEach( path -> this.jmxConfigs.put( path, a ) ) );
	}
}
