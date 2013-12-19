package interprete;

import gui.Consola;

import java.util.List;

import codigoP.Instruccion;
import codigoP.Instruccion_op;

public class VM {
	int pc, sp, sl;
	int stack[]=new int[256];
	Instruccion m[];
	boolean stop;
	public Consola out=null;
	boolean DEBUG;
	public Instruccion last_exec;
	
	public void load(List<Instruccion> program){
		restart();
		m=program.toArray(new Instruccion[program.size()]);
		return;
	}
	
	public boolean stoped() {
		return stop;
	}
	
	public int stackSize() {
		return stack.length;
	}
	
	public int getFormStack(int p){
		if(p<0||p>=stack.length)return 0;
		return stack[p];
	}
	
	public int[] getNewParams(){
		if(last_exec==null||last_exec.opCode()!=0x61)return null;
		int n=((Instruccion_op)last_exec).operando();
		int [] par= new int[n];
		for(int i=0;i<n;i++)
			par[i]=stack[sl+i+1];
		return par;
	}
	
	public int[] getReg(){
		return new int[]{sp,sl,pc,(stop)?1:0};
	}
	
	public boolean run(double seg){
		long msgs=(long)seg*1000;
		try{
			while(!stop){
				safe_exe();
				Thread.sleep(msgs);
			}
		}catch(InterruptedException e){
			write(e.toString(),2);
			return false;
		}
		return true;
	}
	
	public void restart(){
		pc=0; 
		sp=sl=-1;
		last_exec=null;
		stop=false;
	}
	public void safe_exe(){
		try{
			exe();
		}catch(ErrorEjecucion e){}
	}
	public void exe() throws ErrorEjecucion{
		if(m==null){
			stop=true;
			write("No program loaded.",0);
			return;
		}
		try {
			if(pc<0||pc>=m.length)
				throw new ErrorEjecucion("Access violation", pc);
			
			if(DEBUG)
				write("["+pc+"]:\t"+m[pc],0);
		
			ejecuta(m[pc]);
		
		} catch (ErrorEjecucion e) {
			writeAndThrow(e);
		}catch(java.lang.ArrayIndexOutOfBoundsException a){
			writeAndThrow(new ErrorEjecucion("Stack overflow "+a.getMessage(), pc));
		}catch(java.lang.ArithmeticException d){
			writeAndThrow(new ErrorEjecucion(d.getMessage(), pc));
		}
	}
	
	private void writeAndThrow(ErrorEjecucion e)throws ErrorEjecucion{
		stop=true;
		write(e.toString(), 2);
		throw e;
	}
	
	protected void ejecuta(Instruccion ins)throws ErrorEjecucion {
		int op=0;
		if(ins instanceof Instruccion_op)
			op=((Instruccion_op) ins).operando();
		switch(ins.opCode()){
			case 0x00://stop
				stop=true;
				break;
			case 0x10://push
				stack[++sp]=op;
				pc++;
				break;
			case 0x11://push_param
				sp++;
				stack[sp]=stack[sl+op];
				pc++;
				break;
			case 0x12://copy
				sp++;
				stack[sp]=stack[sp-1];
				pc++;
				break;
			case 0x20://pop
				sp--;
				pc++;
				break;
			case 0x30://lt
				stack[sp-1]=(stack[sp-1]<stack[sp])?1:0;
				sp--;
				pc++;
				break;
			case 0x31://gt
				stack[sp-1]=(stack[sp-1]>stack[sp])?1:0;
				sp--;
				pc++;
				break;
			case 0x32://le
				stack[sp-1]=(stack[sp-1]<=stack[sp])?1:0;
				sp--;
				pc++;
				break;
			case 0x33://ge
				stack[sp-1]=(stack[sp-1]>=stack[sp])?1:0;
				sp--;
				pc++;
				break;
			case 0x34://ne 
				stack[sp-1]=(stack[sp-1]!=stack[sp])?1:0;
				sp--;
				pc++;
				break;
			case 0x35://eq
				stack[sp-1]=(stack[sp-1]==stack[sp])?1:0;
				sp--;
				pc++;
				break;
			case 0x40://add
				stack[sp-1]=stack[sp-1]+stack[sp];
				sp--;
				pc++;
				break;
			case 0x41://sub
				stack[sp-1]=stack[sp-1]-stack[sp];
				sp--;
				pc++;
				break;
			case 0x42://mul
				stack[sp-1]=stack[sp-1]*stack[sp];
				sp--;
				pc++;
				break;
			case 0x43://div
				stack[sp-1]=stack[sp-1]/stack[sp];
				sp--;
				pc++;
				break;
			case 0x44://mod
				stack[sp-1]=stack[sp-1]%stack[sp];
				sp--;
				pc++;
				break;
			case 0x45://inv
				stack[sp]=-stack[sp];
				pc++;
				break;
			case 0x50://and
				if(stack[sp-1]!=0)
					stack[sp-1]=stack[sp];
				sp--;
				pc++;
				break;
			case 0x51://or
				if(stack[sp-1]==0)
					stack[sp-1]=stack[sp];
				sp--;
				pc++;
				break;
			case 0x52://not
				stack[sp]=(stack[sp]==0)?1:0;
				pc++;
				break;
			case 0x60://pre_call
				stack[++sp]=op;
				stack[++sp]=sl;
				pc++;
				break;
			case 0x61://pos_call
				sl=sp-op;
				sp++;
				stack[sp]=pc+1;
				pc=stack[sl-1];
				break;
			case 0x62://return
				pc=stack[sp-1];
				stack[sl-1]=stack[sp];
				sp=sl-1;
				sl=stack[sl];
				break;
			case 0x70://goto
				pc=op;
				break;
			case 0x71://bz
				sp--;
				pc=(stack[sp+1]==0)?op:pc+1;
				break;
			case 0x72: //bnz
				sp--;
				pc=(stack[sp+1]!=0)?op:pc+1;
				break;
			case (byte)0x80://write
				write("[ProgramExit]: "+stack[sp]+'\n',1);
				pc++;
				break;
			default://nop
				pc++;
		}
		last_exec=ins;
	}

	private void write(String msg, int c){
		if(out==null)
			System.out.println(msg);
		else
			if(c==2)out.writeErr(msg);
			else if(c==1) out.writeApp(msg);
			else if(c==0) out.write(msg);
	}

	public static void main(String a[]){
		javax.swing.JFileChooser jfc = new javax.swing.JFileChooser("../pruebas");
		jfc.showDialog(new javax.swing.JFrame(), "Ejecutar");
		if(jfc.getSelectedFile()==null)System.exit(0);
		String path= jfc.getSelectedFile().getAbsolutePath();
		System.out.println(path);
		traductor.Traductor as=new traductor.Traductor();
		VM vm = new VM();
		try {
			as.procesa(traductor.lexico.AnalizadorLexico.tokenizeFile(path));
		} catch (java.io.FileNotFoundException e) {}
		if(as.err.isEmpty()){
			vm.load(as.cod);
			vm.DEBUG=true;
			vm.run(0);

		}else System.out.println(as.err+"\n"+as.wrng);
			
		System.exit(0);
	}

}
