
package ascelion.config.impl;

@Deprecated
abstract class Evaluable extends ExpressionItem
{

	abstract CachedItem eval( ConfigNodeImpl node );

	abstract boolean isEvaluable();
}
