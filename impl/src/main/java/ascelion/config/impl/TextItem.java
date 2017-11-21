
package ascelion.config.impl;

final class TextItem extends Evaluable
{

	final String text;

	TextItem( String text )
	{
		this.text = text;
	}

	@Override
	public String toString()
	{
		return this.text;
	}

	@Override
	CachedItem eval( ConfigNodeImpl node )
	{
		return new CachedItem( this.text, node );
	}

	@Override
	boolean isEvaluable()
	{
		return false;
	}
}
