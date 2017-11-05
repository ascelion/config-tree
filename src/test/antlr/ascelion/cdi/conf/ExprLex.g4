lexer grammar ExprLex;

@lexer::header {
	package ascelion.cdi.conf;
}

BEG : '${' ;
END : '}' ;
DEF : ':' ;
STR : (~[\u0000-\u001F${}:\u007f-\uffff])+ ;

