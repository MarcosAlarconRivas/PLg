package traductor.lexico;

public enum TipoToken {
	AMPRESAN,	// &
	NUM,		// 0│{digitoPos}{digito}*
	IDEN,		// letra(letra|digito|_)*
	FUN,		// "fun"
	COMENTARIO,	// #any_char* nl
	OPKEY,		// {
	CLKEY,		// }
	OPPAR,		// (
	CLPAR,		// )
	MAS,		// +
	MENOS,		// -
	POR,		// *
	ENTRE,		// /
	MOD,		// %
	GT,			// >
	LT,			// <
	GEQ,		// >=
	LEQ,		// <=
	NEQ,		// <>
	AND,		// &&
	OR,			// ||
	NOT,		// ~
	THEN,		// ?
	ELSE,		// :
	COMA,		// ,
	EOF,		// \0
	ERROR		//  cualquier otra cosa
}
