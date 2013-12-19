package interprete;

public class ErrorEjecucion extends Exception {
	private static final long serialVersionUID = 1L;
	String msg;
	int  pc;
	public ErrorEjecucion(String msg, int pc){
		this.msg=msg;
		this.pc=pc;
	}
	public String toString(){
		return "[ErrorEjecucion]: "+msg+" (at: "+pc+")\n";
	}
}
