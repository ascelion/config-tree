grammar Expression;

@header {
	package ascelion.cdi.conf;
}

expr
	: START expr DEFAULT expr END
	| START expr END
	| path
	| EOF
	;

path: ITEM DOT path
	| ITEM
	;

START: '${' ;
END: '}' ;
DEFAULT: ':' ;
DOT: '.' ;

ITEM: CHARACTER (CHARACTER | NUMERIC )* ;

CHARACTER : 'A'..'Z' | 'a'..'z' ;
NUMERIC: '0'..'9';
