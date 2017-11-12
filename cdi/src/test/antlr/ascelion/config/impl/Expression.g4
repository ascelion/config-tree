grammar Expression;

@lexer::header {
	package ascelion.config.impl;
}

@parser::header {
	package ascelion.config.impl;

	import static java.util.Collections.unmodifiableList;
	import static java.lang.String.format;

	import java.util.List;
	import java.util.ArrayList;
	import java.util.function.Consumer;
}

@parser::members {
	static public ExpressionParser createFor( String value )
	{
		return createFor( value, x -> {} );
	}

	static public ExpressionParser createFor( String value, Consumer<String> feedback )
	{
//		final boolean[] wrapped = new boolean[] { false };
//		final int length = value.length();
//
//		if( !value.contains( "${" ) ) {
//			value = "${" + value + "}";
//
//			wrapped[0] = true;
//		}

		final CharStream cs = CharStreams.fromString( value );
		final ExpressionLexer lx = new ExpressionLexer( cs );
		final CommonTokenStream ts = new CommonTokenStream( lx );
		final ExpressionParser px = new ExpressionParser( ts, feedback );
		final ANTLRErrorListener el = new BaseErrorListener()
		{

			@Override
			public void syntaxError( Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e )
			{
//				if( wrapped[0] ) {
//					charPositionInLine -= 2;
//					
//					if( charPositionInLine < 0 ) {
//						charPositionInLine = 0;
//					} 
//				}

				px.addError( new EvalError( msg, charPositionInLine ) );
			}
		};

		lx.getErrorListeners().clear();
		px.getErrorListeners().clear();

		lx.addErrorListener( el );
		px.addErrorListener( el );

		return px;
	}

	private final List<EvalError> errors = new ArrayList<>();
	private Consumer<String> feedback = x->{};

	private ExpressionParser( TokenStream ts, Consumer<String> feedback )
	{
		this( ts );
		this.feedback = feedback;
	}

	public List<EvalError> getErrors()
	{
		return unmodifiableList(this.errors);
	}

	public void addError(EvalError error)
	{
		feedback.accept(format("Error: at=%d, text=%s", error.position, error.message));

		this.errors.add(error);
	}
	
	public int getNumberOfSyntaxErrors() {
		return errors.size();
	}
}

root
	: expr+ EOF
	;

expr
	: STR expr*
	| expr DEF expr
	| BEG expr END
	;

BEG : '${' ;
END : '}' ;
DEF : ':' ;
//STR : (~[\u0000-\u001F${}:\u007f-\uffff])+ ;
STR : ('0'..'9'|'A'..'Z'|'a'..'z'|'-' |'_'|'.'|','|';'|'~'|' ')+ ;

