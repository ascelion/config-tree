
package ascelion.config.cvt;

import java.lang.reflect.Type;

import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.context.GenericsContext;

abstract class TypeRef<T>
{

	private final Type type;

	TypeRef()
	{
		final GenericsContext c1 = GenericsResolver.resolve( getClass() );
		final GenericsContext c2 = c1.type( TypeRef.class );

		this.type = c2.genericType( 0 );
	}

	Type type()
	{
		return this.type;
	}
}
