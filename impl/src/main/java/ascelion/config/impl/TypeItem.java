
package ascelion.config.impl;

import ascelion.config.impl.ItemTokenizer.Token;

class TypeItem extends ExpressionItem
{

	final Token.Type type;

	TypeItem( ItemTokenizer.Token.Type type )
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return this.type.value;
	}
}
