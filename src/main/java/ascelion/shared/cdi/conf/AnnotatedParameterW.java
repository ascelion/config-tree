
package ascelion.shared.cdi.conf;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

import static java.lang.String.format;

class AnnotatedParameterW<X> extends AnnotatedW implements AnnotatedParameter<X>
{

	private final AnnotatedParameter<X> delegate;

	AnnotatedParameterW( AnnotatedParameter<X> delegate )
	{
		super( delegate );

		this.delegate = delegate;
	}

	@Override
	public int getPosition()
	{
		return this.delegate.getPosition();
	}

	@Override
	public AnnotatedCallable<X> getDeclaringCallable()
	{
		return this.delegate.getDeclaringCallable();
	}

	@Override
	String name()
	{
		return format( "arg%d", getPosition() );
	}
}
