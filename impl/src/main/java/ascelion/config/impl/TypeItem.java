
package ascelion.config.impl;

@Deprecated
class TypeItem extends ExpressionItem
{

	final Token.Type type;

	TypeItem( Token.Type type )
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return this.type.value;
	}
}
