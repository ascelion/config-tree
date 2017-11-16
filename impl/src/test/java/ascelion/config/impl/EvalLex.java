
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.List;

import ascelion.config.api.ConfigNode;
import ascelion.config.impl.Eval.Expr;
import ascelion.config.impl.Eval.Rule;
import ascelion.config.impl.Eval.Text;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

public class EvalLex
{

	static Rule parse( String content )
	{
		final CharStream cs = CharStreams.fromString( content );
		final ExprLex lx = new ExprLex( cs );
		final List<EvalError> errors = new ArrayList<>();

		lx.addErrorListener( new BaseErrorListener()
		{

			@Override
			public void syntaxError( Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e )
			{
				errors.add( new EvalError( msg, charPositionInLine ) );
			}
		} );

		Token tk = null;
		boolean eof = false;
		Expr root = new Expr();

		while( !eof ) {
			tk = lx.nextToken();

			switch( tk.getType() ) {
				case Recognizer.EOF:
					eof = true;
				break;

				case ExprLex.BEG:
					root = root.push( new Expr() );
				break;

				case ExprLex.END:
					if( root.parent == null ) {
						errors.add( new EvalError( "unbalanced '}'", lx.getCharPositionInLine() ) );

						eof = true;

						break;
					}

					root = root.pop();
				break;

				case ExprLex.DEF:
					root.toDefault();
				break;

				case ExprLex.STR:
					root.push( new Text( tk.getText() ) );
				break;
			}
		}

		if( root.parent != null ) {
			errors.add( new EvalError( "unbalanced '${'", lx.getCharPositionInLine() ) );
		}
		if( errors.size() > 0 ) {
			throw new EvalException( content, errors );
		}

		if( root.val.stream().allMatch( Text.class::isInstance ) ) {
			final Expr expr = new Expr();

			expr.push( root );

			root = expr;
		}

		return root;
	}

	static String eval( String value, ConfigNode root )
	{
		final Rule rule = parse( value );

		return rule.eval( root, EvalLex::parse );
	}

}
