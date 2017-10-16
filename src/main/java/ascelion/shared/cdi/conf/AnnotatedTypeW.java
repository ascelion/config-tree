
package ascelion.shared.cdi.conf;

import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

class AnnotatedTypeW<X> extends AnnotatedW implements AnnotatedType<X>
{

	private final AnnotatedType<X> delegate;
	private final Set<AnnotatedConstructor<X>> constructors;
	private final Set<AnnotatedField<? super X>> fields;
	private final Set<AnnotatedMethod<? super X>> methods;

	AnnotatedTypeW( AnnotatedType<X> delegate )
	{
		super( delegate );

		this.delegate = delegate;

		this.constructors = delegate.getConstructors().stream().map( AnnotatedConstructorW::new ).collect( Collectors.toSet() );
		this.fields = delegate.getFields().stream().map( AnnotatedFieldW::new ).collect( Collectors.toSet() );
		this.methods = delegate.getMethods().stream().map( AnnotatedMethodW::new ).collect( Collectors.toSet() );
	}

	@Override
	public Class<X> getJavaClass()
	{
		return this.delegate.getJavaClass();
	}

	@Override
	public Set<AnnotatedConstructor<X>> getConstructors()
	{
		return this.constructors;
	}

	@Override
	public Set<AnnotatedMethod<? super X>> getMethods()
	{
		return this.methods;
	}

	@Override
	public Set<AnnotatedField<? super X>> getFields()
	{
		return this.fields;
	}

	@Override
	String name()
	{
		return getBaseType().getTypeName();
	}

}
