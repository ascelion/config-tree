
package ascelion.config.impl;

import ascelion.config.impl.ItemTokenizer.Token;

abstract class ItemParserListener<T>
{

	void start()
	{
	}

	void seen( Token tok )
	{
	}

	T finish()
	{
		return null;
	}

}
