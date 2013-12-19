package codigoP;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class ByteCode {
	
	public static void saveAsFile(FileOutputStream fos, List<Instruccion>program){
		try{
			for(Instruccion ins:program)
				fos.write(ins.toByteCode());
			fos.close();
		}catch(Exception e){}
	}
	
	@SuppressWarnings("deprecation")
	public static LinkedList<Instruccion> asList(FileInputStream fis){
		LinkedList<Instruccion> output= new LinkedList<Instruccion>();
		DataInputStream d= new DataInputStream(fis);
		while(true){
			try {
				if(fis.available()<1){
					fis.close();
					return output;
				}
				byte opC = (byte) fis.read();
				Instruccion ins;
				switch(opC){
					case 0x00: ins=Instruccion.stop();break;
					case 0x10: ins=Instruccion.push(get_lit(d));break;
					case 0x11: ins=Instruccion.push_param(get_par(d));break;
					case 0x12: ins=Instruccion.copy();break;
					case 0x20: ins=Instruccion.pop();break;
					case 0x30: ins=Instruccion.lt();break;
					case 0x31: ins=Instruccion.gt();break;
					case 0x32: ins=Instruccion.le();break;
					case 0x33: ins=Instruccion.ge();break;
					case 0x34: ins=Instruccion.ne();break;
					case 0x35: ins=Instruccion.eq();break;
					case 0x40: ins=Instruccion.add();break;
					case 0x41: ins=Instruccion.sub();break;
					case 0x42: ins=Instruccion.mul();break;
					case 0x43: ins=Instruccion.div();break;
					case 0x44: ins=Instruccion.mod();break;
					case 0x45: ins=Instruccion.inv();break;
					case 0x50: ins=Instruccion.and();break;
					case 0x51: ins=Instruccion.or();break;
					case 0x52: ins=Instruccion.not();break;
					case 0x60: ins=Instruccion.pre_call(get_dir(d));break;
					case 0x61: ins=Instruccion.post_call(get_par(d));break;
					case 0x62: ins=Instruccion.return_();break;
					case 0x70: ins=Instruccion.goto_(get_dir(d));break;
					case 0x71: ins=Instruccion.bz(get_dir(d));break;
					case 0x72: ins=Instruccion.bnz(get_dir(d));break;
					case (byte)0x80: ins=Instruccion.write();break;
					default:
						ins=new Instruccion("unknown", opC);
				}
				output.add(ins);
			} catch (IOException e) {
				System.out.println("Error al leer el archivo "+fis.toString());
				try {fis.close();}catch(Exception x){}
				return output;
			}
		}
	}
	
	private static int get_par(DataInputStream d) throws IOException {
		return d.readUnsignedByte();
	}
	
	protected static int get_dir(DataInputStream d) throws IOException {
		return Math.max(d.readInt(), 0);
	}

	protected static int get_lit(DataInputStream d) throws IOException {
		return d.readInt();
	}
	
	static byte[] InsToByteCode(Instruccion i){
		 return new byte[]{i.opCode};
	}
	
	static byte[] InsToByteCode(Instruccion_op i){
		byte op= (byte)i.opCode;
		int lit= i.operando;
		if(op!=0x10 &&lit<0)lit=0;
		
		if(op==0x11||op==0x61){
			if(lit>127)lit-=256;
			return new byte[] {op, (byte)lit};
		}
		byte r[];
		if(op==0x10)
			r= new byte[] {
			 	op,
	            (byte)(lit >> 24),
	            (byte)(lit >> 16),
	            (byte)(lit >> 8),
	            (byte)lit};
		
		else r= new byte[] {
		 	op,
            (byte)(lit >>> 24),
            (byte)(lit >>> 16),
            (byte)(lit >>> 8),
            (byte)lit};
		return r;
	}
	
	public static void main(String args[]){
		javax.swing.JFileChooser jfc = new javax.swing.JFileChooser("../pruebas/bin");
		jfc.showDialog(new javax.swing.JFrame(), "File");
		if(jfc.getSelectedFile()==null)System.exit(0);
		String path= jfc.getSelectedFile().getAbsolutePath();
		try {
			FileOutputStream out = new FileOutputStream(jfc.getSelectedFile());
			System.out.println(path);
			out.write(Instruccion.bnz(17).toByteCode());//72
			out.write(Instruccion.bnz(72).toByteCode());//72
			out.write(Instruccion.bnz(128).toByteCode());//72
			out.write(Instruccion.bnz(1023).toByteCode());//72
			out.write(Instruccion.bnz(8192).toByteCode());//72
			out.write(Instruccion.bnz(8192+512).toByteCode());//72
			out.write(Instruccion.push(0).toByteCode());//10
			out.write(Instruccion.push(1).toByteCode());
			out.write(Instruccion.push(12345).toByteCode());
			out.write(Instruccion.push(-10000).toByteCode());
			out.write(Instruccion.push(4096).toByteCode());
			out.write(Instruccion.push(Integer.MAX_VALUE).toByteCode());
			out.write(Instruccion.push(Integer.MIN_VALUE).toByteCode());
			out.write(Instruccion.push_param(248).toByteCode());//11
			out.write(Instruccion.push_param(0).toByteCode());//11
			out.write(Instruccion.push_param(123).toByteCode());//11
			out.write(Instruccion.push_param(255).toByteCode());//11
			out.write(Instruccion.stop().toByteCode());//00
			out.close();
			FileInputStream in = new FileInputStream(jfc.getSelectedFile());
			java.util.List<Instruccion> list =asList(in);
			int i=0;
			for(Instruccion ins:list)
				System.out.println(i+++":\t"+ins);
			in.close();
		}catch(Exception e){
			System.out.println(e);
		}
		System.exit(0);
	}

}
