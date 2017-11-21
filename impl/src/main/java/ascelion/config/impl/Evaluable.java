
package ascelion.config.impl;

abstract class Evaluable extends ExpressionItem
{

	abstract CachedItem eval( ConfigNodeImpl node );

	abstract boolean isEvaluable();
}
