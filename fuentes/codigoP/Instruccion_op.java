package codigoP;

public class Instruccion_op extends Instruccion{
	int operando;
	public Instruccion_op(String n, byte opCod, int oper){
		super(n, opCod);
		operando=oper;
	}
	public void parchea(int dir){
		operando=dir;
	}
	public int operando(){
		return operando;
	}
	public String toString(){
		return name+'('+operando+')';
	}
	public byte[] toByteCode(){
		return ByteCode.InsToByteCode(this);
	}
	
}