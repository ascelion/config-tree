
package ascelion.shared.cdi.conf;

import java.lang.reflect.Method;

import javax.enterprise.inject.spi.AnnotatedMethod;

final class AnnotatedMethodW<X> extends AnnotatedCallableW<X> implements AnnotatedMethod<X>
{

	private final AnnotatedMethod<X> delegate;

	AnnotatedMethodW( AnnotatedMethod<X> delegate )
	{
		super( delegate );

		this.delegate = delegate;
	}

	@Override
	public Method getJavaMember()
	{
		return this.delegate.getJavaMember();
	}
}
