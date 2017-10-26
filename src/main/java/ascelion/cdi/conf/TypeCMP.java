
package ascelion.cdi.conf;

import java.lang.reflect.Type;
import java.util.Comparator;

final class TypeCMP<T extends Type> implements Comparator<T>
{

	@Override
	public int compare( T o1, T o2 )
	{
		if( o1 == o2 ) {
			return 0;
		}
		if( o1 == null || o2 == null ) {
			return o1 == null ? -1 : +1;
		}

		return o1.getTypeName().compareTo( o2.getTypeName() );
	}

}
