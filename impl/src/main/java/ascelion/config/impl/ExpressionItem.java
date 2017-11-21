
package ascelion.config.impl;

import java.util.Objects;

abstract class ExpressionItem
{

	Expression parent;

	@Override
	public boolean equals( Object obj )
	{
		if( obj == this ) {
			return true;
		}
		if( obj == null ) {
			return false;
		}

		if( getClass() != obj.getClass() ) {
			return false;
		}

		final ExpressionItem that = (ExpressionItem) obj;

		return Objects.equals( toString(), that.toString() );
	}
}
