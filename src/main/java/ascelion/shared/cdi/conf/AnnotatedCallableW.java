
package ascelion.shared.cdi.conf;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

import static java.lang.String.format;

abstract class AnnotatedCallableW<X> extends AnnotatedMemberW<X> implements AnnotatedCallable<X>
{

	private final AnnotatedCallable<X> delegate;
	private final List<AnnotatedParameter<X>> parameters;

	AnnotatedCallableW( AnnotatedCallable<X> delegate )
	{
		super( delegate );

		this.delegate = delegate;
		this.parameters = delegate.getParameters().stream().map( AnnotatedParameterW::new ).collect( Collectors.toList() );
	}

	@Override
	public List<AnnotatedParameter<X>> getParameters()
	{
		return this.parameters;
	}

	@Override
	String name()
	{
		return format( "%s(%s)", super.name(), this.parameters.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) );
	}

}
