package traductor;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import traductor.ts.TablaSimbolos;
import codigoP.Instruccion;
import codigoP.Instruccion_op;
import traductor.lexico.TipoToken;
import traductor.lexico.Token;
/**
 * Traductor predictivo recursivo de la practica de Septiembre 2013
 */
public class Traductor {
	private boolean DEBUG=false;
	public LinkedList<ErrorCompilacion> err;
	public LinkedList<Warning> wrng;
	public TablaSimbolos ts;
	public LinkedList<Instruccion> cod;
	protected ListIterator<Token> tokens;
	protected Token current;
	//private Token consumed_as_error=null;
	
	
	public static Traductor run(List<Token> tokens){
		Traductor as = new Traductor();
		as.procesa(tokens);
		return as;
	}
	
	public static Traductor runTracing(List<Token> tokens){
		Traductor as = new Traductor();
		as.DEBUG=true;
		as.procesa(tokens);
		as.DEBUG=false;
		return as;
	}
	
	public void procesa(List<Token> tokenList){
		err = new LinkedList<ErrorCompilacion>();
		wrng = new LinkedList<Warning>();
		for(Token t:tokenList)
			if(t.getTipo()==TipoToken.ERROR)
				err.add(new ErrorLexico(t));
		
		if(err.isEmpty()){
			cod = new LinkedList<Instruccion>();
			ts = new TablaSimbolos();
			tokens= tokenList.listIterator();
		
			next();//carga el primer token
		
			prog();
			reconoce(TipoToken.EOF);
		}
		
		if(!wrng.isEmpty())debug(wrng.toString());
		if(DEBUG&&err.isEmpty()){
			int i=0;
			for(Instruccion ins:cod)
				debug(i+++":\t"+ins);
		}else
			debug(err.toString());
		
	}
	
	/*-----------------Gramatica de atributos-----------------*/
	
	private void prog() {
		debug("Prog ::= Decs & Exp");
		Instruccion_op main=Instruccion.goto_(0);
		cod.add(main);//salto a la rutina main
		
		decs();
		
		if(current_tipo(TipoToken.AMPRESAN)) reconoce(TipoToken.AMPRESAN);
		else err.add(new ErrorIncontextual(current, "'&' or 'fun'"));
		
		if(cod.size()>1) main.parchea(cod.size());
		else cod.removeFirst();
		
		exp();
		cod.add(Instruccion.write());
		cod.add(Instruccion.stop());
	}

	private void decs() {
		while(current_tipo(TipoToken.FUN)){
			debug("Decs ::= Dec Decs");
			dec();
		}
		debug("Decs ::= λ");
	}

	private void dec() {
		reconoce(TipoToken.FUN);
		String idFUN =reconoce(TipoToken.IDEN).getLex();
		debug("Dec ::= fun "+idFUN+"(Params){Exp}");
		if(ts.existeFun(idFUN))
			contextual("Dulicated function "+idFUN);
		
		reconoce(TipoToken.OPPAR);
		List<String>idParams = params();
		reconoce(TipoToken.CLPAR);
		
		ts.declaraFun(idFUN, idParams, cod.size());
		reconoce(TipoToken.OPKEY);
		ts.call(idFUN);
		exp();
		cod.add(Instruccion.return_());
		ts.return_();
		reconoce(TipoToken.CLKEY);
	}
	
	private List<String> params() {
		LinkedList<String> lista= new LinkedList<String>();
		if(current_tipo(TipoToken.CLPAR)){
			debug("Params ::= λ");
			return lista;
		}
		debug("Params ::= Param RParam");
		chekAndAdd(lista, reconoce(TipoToken.IDEN));
		
		while(current_tipo(TipoToken.COMA)){
			reconoce(TipoToken.COMA);
			debug("RParam ::= , Param RParam");
			chekAndAdd(lista, reconoce(TipoToken.IDEN));
		}
		debug("RParam ::= λ");
		return lista;
	}

	private void exp() {
		debug("Exp ::=  Exp1 | Exp1 ? Exp : Exp");
		exp1();
		if(current_tipo(TipoToken.THEN)){
			reconoce(TipoToken.THEN);
			Instruccion_op ir_f, jump;
			cod.add(ir_f=Instruccion.bz(0));
			exp();
			cod.add(jump=Instruccion.goto_(0));
			
			reconoce(TipoToken.ELSE);
			ir_f.parchea(cod.size());
			exp();
			jump.parchea(cod.size());
		}		
	}

	private void exp1() {
		debug("Exp1 ::= Exp2 | Exp2 Op1 Exp2");
		exp2();
		if(current_tipo(TipoToken.LT)){
			reconoce(TipoToken.LT);
			exp2();
			cod.add(Instruccion.lt());
		}else if(current_tipo(TipoToken.GT)){
			reconoce(TipoToken.GT);
			exp2();
			cod.add(Instruccion.gt());
		} else if(current_tipo(TipoToken.LEQ)){
			reconoce(TipoToken.LEQ);
			exp2();
			cod.add(Instruccion.le());
		}else if(current_tipo(TipoToken.GEQ)){
			reconoce(TipoToken.GEQ);
			exp2();
			cod.add(Instruccion.ge());
		}else if(current_tipo(TipoToken.NEQ)){
			reconoce(TipoToken.NEQ);
			exp2();
			cod.add(Instruccion.ne());
		}
	}

	private void exp2() {
		debug("Exp2 ::= Exp3 RExp2");
		exp3();
		rexp2();
	}

	private void rexp2() {
		List<Instruccion_op> lv= new LinkedList<Instruccion_op>();
		while(true)//mientras encuentres +, - u ||
			if(current_tipo(TipoToken.MAS)){
				debug("RExp2 :: + Exp2");
				reconoce(TipoToken.MAS);
				exp3();
				cod.add(Instruccion.add());
			}else if(current_tipo(TipoToken.MENOS)){
				debug("RExp2 :: - Exp2");
				reconoce(TipoToken.MENOS);
				exp3();
				cod.add(Instruccion.sub());
			} else if(current_tipo(TipoToken.OR)){
				debug("RExp2 :: || Exp2");
				reconoce(TipoToken.OR);
				//cortocircuito
				Instruccion_op ir_v = Instruccion.bnz(0);
				lv.add(ir_v);
				cod.add(Instruccion.copy());
				cod.add(ir_v);
				cod.add(Instruccion.pop());
				exp3();
				//cod.add(Instruccion.or());
			}else{
				debug("RExp2 :: λ");
				parchea(lv, cod.size());
				break;
			}
	}
	
	private void exp3() {
		debug("Exp3 ::= Exp4 RExp3");
		exp4();
		rexp3();
	}

	private void rexp3() {
		List<Instruccion_op> lf= new LinkedList<Instruccion_op>();
		while(true)//mietras encuentres *, /, % o &&
			if(current_tipo(TipoToken.POR)){
				debug("RExp3 :: * Exp3");
				reconoce(TipoToken.POR);
				exp4();
				cod.add(Instruccion.mul());
			}else if(current_tipo(TipoToken.ENTRE)){
				debug("RExp3 :: / Exp3");
				reconoce(TipoToken.ENTRE);
				exp4();
				cod.add(Instruccion.div());
			} else if(current_tipo(TipoToken.MOD)){
				debug("RExp3 :: % Exp3");
				reconoce(TipoToken.MOD);
				exp4();
				cod.add(Instruccion.mod());
			} else if(current_tipo(TipoToken.AND)){
				debug("RExp3 :: && Exp3");
				reconoce(TipoToken.AND);
				//cortocircuito
				Instruccion_op ir_f = Instruccion.bz(0);
				lf.add(ir_f);
				cod.add(Instruccion.copy());
				cod.add(ir_f);
				cod.add(Instruccion.pop());
				exp4();
				//cod.add(Instruccion.and());
			}else{
				debug("RExp3 :: λ");
				parchea(lf, cod.size());
				break;
			}
	}

	private void exp4() {
		if(current_tipo(TipoToken.NOT)){
			debug("Exp4 ::= ~ Exp4");
			reconoce(TipoToken.NOT);
			exp4();
			cod.add(Instruccion.not());
		}else if(current_tipo(TipoToken.MENOS)){
			debug("Exp4 ::= - Exp4");
			reconoce(TipoToken.MENOS);
			exp4();
			cod.add(Instruccion.inv());
		}else{
			debug("Exp4 ::= Literal");
			literal();
		}
	}

	private void literal() {
		if(current_tipo(TipoToken.OPPAR)){//(
			debug("Literal ::= (Exp)");
			reconoce(TipoToken.OPPAR);
			exp();
			reconoce(TipoToken.CLPAR);
		}else if(current_tipo(TipoToken.NUM)){//num
			debug("Literal ::= num");
			int num;
			try{
				num=Integer.parseInt(reconoce(TipoToken.NUM).getLex());
			}catch(java.lang.NumberFormatException e){
				err.add(new ErrorCompilacion(e, current));
				num=Integer.MAX_VALUE;
			}
			cod.add(Instruccion.push(num));
			
		}else if(current_tipo(TipoToken.IDEN)||current_tipo(TipoToken.FUN)&&ts.existeID("fun")){
			String iden=reconoce(TipoToken.IDEN).getLex();
			
			if(current_tipo(TipoToken.OPPAR)){//fun
				debug("Literal ::="+iden+"(Args)");
				reconoce(TipoToken.OPPAR);
				
				boolean existe=ts.existeFun(iden);
				if(!existe)
					contextual("Undeclared function "+(ts.existeParam(iden)?", may be you mean the param ":"")+iden);
					
				cod.add(Instruccion.pre_call(ts.etqh(iden)));
				
				int exp=ts.aridad(iden);
				int	fnd=args();
				if(exp!=fnd && existe)
					contextual("Expected "+exp+" args in fun "+iden+", but "+fnd+" found.");
				
				cod.add(Instruccion.post_call(exp));
				reconoce(TipoToken.CLPAR);
				
			}else{//param
				debug("Literal ::="+iden);
				if(!ts.existeParam(iden))
					if(ts.inMain())
						contextual("Main function has not params "+(ts.existeFun(iden)?", may be you mean the function ":"")+iden);
					else
						contextual("Undeclared param "+(ts.existeFun(iden)?", may be you mean the function ":"")+iden);
				cod.add(Instruccion.push_param(ts.wichParamIs(iden)));
			}
			
		}else {
			incontextual(null);
			next();//FIXME
			if(current!=null&&!current_tipo(TipoToken.EOF))
				literal();
			
		}
		
	}

	private int args() {
		if(current_tipo(TipoToken.CLPAR)){
			debug("Args ::= λ");
			return 0;
		}
		
		debug("Args ::= Exp RArgs");
		int num=1;
		exp();
		
		while(current_tipo(TipoToken.COMA)){
			reconoce(TipoToken.COMA);
			debug("RArgs ::= , Exp RArgs");
			num++;
			exp();
		}
		debug("RArgs ::= λ");
		return num;
	}
	
	/*------------Fin de la Gramatica de atributos------------*/
	
	/**used auxiliary in function params()*/
	protected void chekAndAdd(List<String> lista, Token reconocido){
		if(reconocido==null)return;
		String lex=reconocido.getLex();
		for(String id: lista)
			if(id.compareTo(lex)==0)contextual("Dulicated param "+lex);
		lista.add(lex);
		if(ts.existeFun(lex))
			wrng.add(new Warning("Dulicated symbol "+lex, current));
	}
	
	protected boolean current_tipo(TipoToken t){
		/*if(current!=null&&current.getTipo()==t)return true;
		if(consumed_as_error!=null&&consumed_as_error.getTipo()==t){
			current=tokens.previous();
			consumed_as_error=null;
		}else if(tokens.hasNext()){
			Token n=tokens.next();
			if(n.getTipo()==t){
				//consumed_as_error=current;
				current=n;
			}else tokens.previous();
		}*/
		return current!=null&&current.getTipo()==t;
	}
	
	private Token reconoce(TipoToken tipo) {
		
		if(current_tipo(tipo)){
			debug("processed "+current.getLex()+" /"+current.getLinea());
			Token recogn=current;
			next();
			//consumed_as_error=null;
			return recogn;
		}else if(current_tipo(TipoToken.FUN)&&tipo==TipoToken.IDEN){
			debug("processed "+current.getLex()+" as iden /"+current.getLinea());
			Token recogn=current;
			recogn.setTipo(TipoToken.IDEN);
			next();
			//consumed_as_error=null;
			return recogn;
		}
		
		if(tipo==TipoToken.EOF){
			String ingored="";
			while(current!=null && !current_tipo(TipoToken.EOF)){
				ingored+=current.getLex();
				next();
			}
			wrng.add(new Warning("Ignored symbols "+ingored+" at the end of the program."));
			return current;
		}else {
			incontextual(tipo);
			next();
		}
		return null;
	}
	
	private void parchea(List<Instruccion_op> list, int dir) {
		for(Instruccion_op ir:list)
			ir.parchea(dir);
	}

	protected void incontextual(TipoToken tipo){
		//consumed_as_error=current;
		if(current.getTipo()==TipoToken.ERROR)
			err.add(new ErrorLexico(current));
		else
			//si es null ha venido de literal()
			if(tipo!=null)err.add(new ErrorIncontextual(current,tipo));
			else  err.add(new ErrorIncontextual(current,"Literal"));		
	}
	
	protected void contextual(String msg){
		if(current.getTipo()==TipoToken.ERROR)
			err.add(new ErrorLexico(current));
		else
			err.add(new ErrorContextual(msg, current));
	}
	
	protected Token next(){
		do
			if(!tokens.hasNext()){
				if(current.getTipo()!=TipoToken.EOF)
					debug("Fatal Error: Unspected end of Token List.");
				else
					debug("EOF reached.");
				return null;
		}while((current=tokens.next()).getTipo()==TipoToken.COMENTARIO);
		
		return current;
	}
	
	public void debug(String s){
		if(DEBUG)System.out.println(s);
	}
	
	public static void main(String args[]){
		javax.swing.JFileChooser jfc = new javax.swing.JFileChooser("../pruebas");
		jfc.showDialog(new javax.swing.JFrame(), "Traducir");
		if(jfc.getSelectedFile()==null)System.exit(0);
		String path= jfc.getSelectedFile().getAbsolutePath();
		System.out.println(path);
		try {
			Traductor.runTracing(traductor.lexico.AnalizadorLexico.tokenizeFile(path));
		} catch (java.io.FileNotFoundException e) {}
		System.exit(0);
	}
}

