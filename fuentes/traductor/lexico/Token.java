package traductor.lexico;

public class Token {

	private String lex;
	private String message="";
	private int linea;
	private int columna;
	private TipoToken tipo;

	public static Token error(String lex, int l, int c, String mess){
		Token err = new Token(lex, l, c, TipoToken.ERROR);
		err.message= mess;
		return err;
	}
	
	public Token(String s, int l, int c, TipoToken t){
		lex=s;
		linea=l;
		columna=c;
		tipo=t;
	}
	
	public String getLex(){
		return lex;
	}
	
	public int getLinea(){
		return linea;
	}
	
	public int getCol(){
		return columna;
	}
	
	public int getLength(){
		return lex.length();
	}
	
	public TipoToken getTipo(){
		return tipo;
	}
	
	public void setTipo(TipoToken newTipe){
		tipo=newTipe;
	}
	
	public String msg(){
		return message;
	}
	
	public String toString() {
		return "Token [ "+lex+", line="+linea+", pos:"+columna+ (getLength()>1? ("-"+(columna+getLength())) :"")
				+", tipo="+tipo+' '+ message+ "]";
	}
	
	public static String mostrar(TipoToken t){
		switch(t){
		case AMPRESAN:	return "&";
		case NUM: 		return "number";
		case IDEN:		return "identificador";
		case FUN:		return "\"fun\"";
		case COMENTARIO:return "#";
		case OPKEY:		return "{";
		case CLKEY:		return "}";
		case OPPAR:		return "(";
		case CLPAR:		return ")";
		case MAS:		return "+";
		case MENOS:		return "-";
		case POR:		return "*";
		case ENTRE:		return "/";
		case MOD:		return "%";
		case GT:		return ">";
		case LT:		return "<";
		case GEQ:		return ">=";
		case LEQ:		return "<=";
		case NEQ:		return "<>";
		case AND:		return "&&";
		case OR:		return "||";
		case NOT:		return "~";
		case THEN:		return "?";
		case ELSE:		return ":";
		case COMA:		return ",";
		case EOF:		return "\\0";
		case ERROR:		return "error";
		default:		return "fatal error";
		}
	}
	
}