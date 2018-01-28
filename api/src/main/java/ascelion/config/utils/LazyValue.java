
package ascelion.config.utils;

import java.util.function.Supplier;

public final class LazyValue<T>
{

	private T value;

	public T get( Supplier<T> sup )
	{
		if( this.value != null ) {
			return this.value;
		}

		synchronized( this ) {
			if( this.value != null ) {
				return this.value;
			}

			return this.value = sup.get();
		}
	}
}
