package traductor;

import traductor.lexico.TipoToken;
import traductor.lexico.Token;

public class ErrorCompilacion {
	int linea, columna, length;
	String info;
	protected ErrorCompilacion(){}
	protected ErrorCompilacion(Exception e, Token t){
		this(t);
		info="["+e.getClass().getSimpleName()+"]: "+e.getMessage();
	}
	protected ErrorCompilacion(Token t){
		linea=t.getLinea();
		columna=t.getCol();
		length=t.getLength();
	}
	public String toString(){
		return info+" at line: "+linea;
	}
}

class ErrorLexico extends ErrorCompilacion{
	public ErrorLexico(Token err){
		super(err);
		info="[ErrorLexico]: "+err.msg()+" "+'\"'+err.getLex()+'\"';
	}
}

class ErrorIncontextual extends ErrorCompilacion{
	public ErrorIncontextual(Token found, TipoToken exp){
		this(found, Token.mostrar(exp));
	}
	
	public ErrorIncontextual(Token found, String exp){
		super(found);
		info="[ErrorIncontextual]: expected "+exp+" but "
				+(found.getTipo()==TipoToken.EOF?"\\0":found.getLex())+" found";
	}
}

class ErrorContextual extends ErrorCompilacion{
	public ErrorContextual(String msg, Token err){
		super(err);
		info="[ErrorContextual]: "+msg;
	}
}

class Warning extends ErrorCompilacion{
	boolean hastoken=false;
	public Warning(String msg, Token err){
		super(err);
		hastoken=true;
		info="[Warning]: "+msg;
	}
	public Warning(String msg){
		info="[Warning]: "+msg;
	}
	public String toString(){
		if(hastoken)return super.toString();
		else return info;
	}
}
