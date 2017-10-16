
package ascelion.shared.cdi.conf;

import java.lang.reflect.Field;

import javax.enterprise.inject.spi.AnnotatedField;

final class AnnotatedFieldW<X> extends AnnotatedMemberW<X> implements AnnotatedField<X>
{

	private final AnnotatedField<X> delegate;

	AnnotatedFieldW( AnnotatedField<X> delegate )
	{
		super( delegate );

		this.delegate = delegate;
	}

	@Override
	public Field getJavaMember()
	{
		return this.delegate.getJavaMember();
	}
}
