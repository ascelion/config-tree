
package ascelion.config.eclipse.ext;

import java.lang.reflect.Type;
import java.util.NoSuchElementException;

import lombok.ToString;
import org.eclipse.microprofile.config.Config;

public interface ConfigExt extends Config
{

	@ToString( of = { "value", "undefined" }, doNotUseGetters = true )
	class Value
	{

		private final boolean undefined;
		private final String value;

		public Value( String value )
		{
			this.value = value;
			this.undefined = false;
		}

		public Value()
		{
			this.value = null;
			this.undefined = true;
		}

		public String get()
		{
			if( undefined ) {
				throw new NoSuchElementException();
			}

			return value;
		}

		public boolean undefined()
		{
			return undefined;
		}
	}

	static ConfigExt wrap( Config cf )
	{
		return cf instanceof ConfigExt ? (ConfigExt) cf : new ConfigWrapper( cf );
	}

	String getValue( String propertyName );

	Value getValue( String propertyName, boolean evaluate );

	<T> T convert( String value, Type type );

	default void addChangeListener( ConfigChangeListener cl )
	{
	}

	default void removeChangeListener( ConfigChangeListener cl )
	{
	}
}
