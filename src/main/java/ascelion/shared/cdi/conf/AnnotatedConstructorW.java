
package ascelion.shared.cdi.conf;

import java.lang.reflect.Constructor;

import javax.enterprise.inject.spi.AnnotatedConstructor;

final class AnnotatedConstructorW<X> extends AnnotatedCallableW<X> implements AnnotatedConstructor<X>
{

	private final AnnotatedConstructor<X> delegate;

	AnnotatedConstructorW( AnnotatedConstructor<X> delegate )
	{
		super( delegate );

		this.delegate = delegate;
	}

	@Override
	public Constructor<X> getJavaMember()
	{
		return this.delegate.getJavaMember();
	}
}
