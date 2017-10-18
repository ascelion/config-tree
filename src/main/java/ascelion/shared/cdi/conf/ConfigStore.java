
package ascelion.shared.cdi.conf;

import java.util.Map;
import java.util.TreeMap;

public class ConfigStore
{

	private final ConfigItemImpl root = new ConfigItemImpl( "" );

	public ConfigStore()
	{
		this.root.set( new TreeMap<>() );
	}

	public Map<String, ? extends ConfigItem> get()
	{
		return this.root.getTree();
	}

	public void add( Map<String, ?> map )
	{
		this.root.add( ConfigItemImpl.remap( map ) );
	}

	public void setValue( String key, Object val )
	{
		final String[] keys = key.split( "\\." );

		setValue( this.root, keys, 0, ConfigItemImpl.toItem( "", val ) );
	}

	public ConfigItem getValue( String key )
	{
		final String[] keys = key.split( "\\." );

		return getValue( this.root, keys, 0 );
	}

//	void put( Map<String, ? extends ConfigItem> map )
//	{
//		this.root.set( map );
//	}

	static private void setValue( ConfigItemImpl root, String[] keys, int depth, ConfigItemImpl ci )
	{
		final ConfigItemImpl val = root.tree().computeIfAbsent( keys[depth], ConfigItemImpl::new );

		if( depth == keys.length - 1 ) {
			val.set( ci );
		}
		else {
			setValue( val, keys, depth + 1, ci );
		}
	}

	static private ConfigItemImpl getValue( ConfigItemImpl root, String[] keys, int depth )
	{
		final Map<String, ConfigItemImpl> map = root.getTree();

		if( map == null ) {
			return null;
		}

		final ConfigItemImpl val = map.get( keys[depth] );

		if( val == null ) {
			return null;
		}
		if( depth == keys.length - 1 ) {
			return val;
		}

		return getValue( val, keys, depth + 1 );
	}
}
