package codigoP;

public class Instruccion {
	String name;
	byte opCode;
	
	protected Instruccion(String n, byte opCod){
		name=n;
		opCode=opCod;
	}

	public int opCode(){
		return opCode;
	}
	
	public String toString(){
		return name;
	}
	
	public byte[] toByteCode(){
		return ByteCode.InsToByteCode(this);
	}
	
	public static Instruccion stop(){
		return new Instruccion("stop",(byte) 0x00);
	}
	
	public static Instruccion_op push(int literal){
		return new Instruccion_op("push",(byte) 0x10, literal);
	}
	
	public static Instruccion_op push_param(int n){
		return new Instruccion_op("push_param",(byte) 0x11, n);
	}
	
	public static Instruccion copy(){
		return new Instruccion("copy",(byte) 0x12);
	}

	public static Instruccion pop(){
		return new Instruccion("pop",(byte) 0x20);
	}
	
	public static Instruccion lt(){
		return new Instruccion("lt",(byte) 0x30);
	}

	public static Instruccion gt(){
		return new Instruccion("gt",(byte) 0x31);
	}
	
	public static Instruccion le(){
		return new Instruccion("le",(byte) 0x32);
	}
	
	public static Instruccion ge(){
		return new Instruccion("ge",(byte) 0x33);
	}
	
	public static Instruccion ne(){
		return new Instruccion("ne",(byte) 0x34);
	}
	/**
	 * @deprecated no se usa porque en el enunciado no hay operador =.
	 */
	public static Instruccion eq(){
		return new Instruccion("eq",(byte) 0x35);
	}
	
	public static Instruccion add(){
		return new Instruccion("add",(byte) 0x40);
	}
	
	public static Instruccion sub(){
		return new Instruccion("sub",(byte) 0x41);
	}
	
	public static Instruccion mul(){
		return new Instruccion("mul",(byte) 0x42);
	}
	
	public static Instruccion div(){
		return new Instruccion("div",(byte) 0x43);
	}
	
	public static Instruccion mod(){
		return new Instruccion("mod",(byte) 0x44);
	}
	
	public static Instruccion inv(){
		return new Instruccion("inv",(byte) 0x45);
	}
	/**
	 * @deprecated no se usa por estar impementado en cortocircuito.
	 */
	public static Instruccion and(){
		return new Instruccion("and",(byte) 0x50);
	}
	/**
	 * @deprecated no se usa por estar impementado en cortocircuito.
	 */
	public static Instruccion or(){
		return new Instruccion("or",(byte) 0x51);
	}
	
	public static Instruccion not(){
		return new Instruccion("not",(byte) 0x52);
	}
	
	public static Instruccion_op pre_call(int direccion){
		return new Instruccion_op("pre_call",(byte) 0x60, direccion);
	}

	public static Instruccion_op post_call(int n){
		return new Instruccion_op("post_call",(byte) 0x61, n);
	}

	public static Instruccion return_(){
		return new Instruccion("return",(byte) 0x62);
	}

	public static Instruccion_op goto_(int direccion){
		return new Instruccion_op("goto",(byte) 0x70, direccion);
	}

	public static Instruccion_op bz(int direccion){
		return new Instruccion_op("bz",(byte) 0x71, direccion);
	}
	
	public static Instruccion_op bnz(int direccion){
		return new Instruccion_op("bnz",(byte) 0x72, direccion);
	}
	
	public static Instruccion write(){
		return new Instruccion("write",(byte) 0x80);
	}

}



	
	