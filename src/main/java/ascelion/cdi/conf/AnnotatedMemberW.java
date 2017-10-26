
package ascelion.cdi.conf;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;

abstract class AnnotatedMemberW<X> extends AnnotatedW implements AnnotatedMember<X>
{

	private final AnnotatedMember<X> delegate;

	AnnotatedMemberW( AnnotatedMember<X> delegate )
	{
		super( delegate );

		this.delegate = delegate;
	}

	@Override
	public boolean isStatic()
	{
		return this.delegate.isStatic();
	}

	@Override
	public AnnotatedType<X> getDeclaringType()
	{
		return this.delegate.getDeclaringType();
	}

	@Override
	String name()
	{
		return getJavaMember().getName();
	}
}
