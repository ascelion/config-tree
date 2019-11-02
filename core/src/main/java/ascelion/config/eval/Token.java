package ascelion.config.eval;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class Token {
	enum Type {
		PREFIX, DEFAULT, SUFFIX, LITERAL;
	}

	final Token.Type type;
	final String value;

	Token(Token.Type type) {
		this(type, null);
	}
}
