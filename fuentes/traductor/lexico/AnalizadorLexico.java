package traductor.lexico;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class AnalizadorLexico {
	enum Estado{E0, FUN, NUM, IDEN, COM, EOF}
	
	private BufferedReader file;
	private Estado state;
	private String current;
	private int line, col;
	private int markedL, markedC;
	
	public AnalizadorLexico(BufferedReader f){
		file=f;
		state=Estado.E0;
		current="";
		line=col=markedC=markedL= 1;
	}
	
	private void closeFile() {
		try {
			file.close();
		} catch (IOException e) {}
		
	}
	
	/** 
	 * Shows but not consume the next char in file.
	 */
	public char next(){
		mark(1);
		char rd= read();
		reset();
		return rd;
	}
	
	/**
	 * Reads and consume a new char from the file.
	 */
	public char read(){
		int c = 0;
		try {
			c= file.read();
			col++;
		} catch (IOException e){}
		if(c>-1) return (char)c;
		return '\0';
	}
	
	/**
	 * Makes a mark in file current position where you can return using reset(), 
	 * @param length: Limit on the number of characters that may be read while still preserving the mark.
	 */
	private boolean mark(int length){
		try {
			file.mark(length);
			markedL=line;
			markedC=col;
			} catch (IOException e) {
				return false;
			}
		return true;
	}
	
	/**
	* Resets the stream to the most recent mark.
	*/
	private boolean reset(){
		try {
			file.reset();
			line=markedL;
			col=markedC;
			} catch (IOException e) {
				return false;
			}
		return true;
	}
	
	public boolean test(String s){
		mark(s.length());
		String readed="";
		for(int i=0; i<s.length(); i++){
			readed += read();
		}
		if(s.compareTo(readed)==0){
			current+= s;
			return true;
		}
		reset();
		return false;
	}
	
	public boolean test(char in){
		char rd;
		mark(1);
		rd= read();
		 if(rd==in){
			 current+=rd;
			 return true;
		 }
		reset();
		return false;
	}
	
	public static List<Token> tokenizeFile(String ruta) throws FileNotFoundException{
		BufferedReader f = new BufferedReader(new FileReader(new File(ruta)));
		AnalizadorLexico a= new AnalizadorLexico(f);
		List<Token> tokens = new LinkedList<Token>();
		while(true){
			Token newToken = a.getToken();
			if(newToken==null)break;
			else tokens.add(newToken);
		}
		a.closeFile();
		return tokens;
	}
	
	public Token getToken(){
		
		switch(state){
		case E0:
			char c = read();
			current = ""+c;
			switch(c){
				case '#': state=Estado.COM; break;
				case '<': if(test('>'))return token(TipoToken.NEQ);
						  if(test('='))return token(TipoToken.LEQ);
						  return token(TipoToken.LT);  
				case '>': if(test('='))return token(TipoToken.GEQ);
						  return token(TipoToken.GT);
				case '&': if(test('&'))return token(TipoToken.AND);
						  return token(TipoToken.AMPRESAN);
				case '|' : if(test('|'))return token(TipoToken.OR);
						   else return error("Unknown operator |, maybe you mean ||");
				case '+' : return token(TipoToken.MAS);
				case '-' : return token(TipoToken.MENOS);
				case '*' : return token(TipoToken.POR);
				case '/' : return token(TipoToken.ENTRE);
				case '%' : return token(TipoToken.MOD);
				case '~' : return token(TipoToken.NOT);
				case '?' : return token(TipoToken.THEN);
				case ':' : return token(TipoToken.ELSE);
				case '(' : return token(TipoToken.OPPAR);
				case ')' : return token(TipoToken.CLPAR);
				case '{' : return token(TipoToken.OPKEY);
				case '}' : return token(TipoToken.CLKEY);
				case ',' : return token(TipoToken.COMA);
				case '\0': return token(TipoToken.EOF);
				case '_' : return consumeError('_');
				case '0' :if(!formaIden(next()))return token(TipoToken.NUM);
						   else return consumeError('0');
				case 'f' : if(test("un"))state=Estado.FUN;
						   else state=Estado.IDEN;
						   break;
				default:
					if(Character.isWhitespace(c)){
						if(c=='\n'){line++; col=0;}
					}else if(Character.isDigit(c)){
						state=Estado.NUM;
					}else if(Character.isLetter(c)){
						state=Estado.IDEN;
					}else return consumeError('s');
				}//fin del switch de char
			return getToken();
			
		case FUN: if(!formaIden(next()))return token(TipoToken.FUN);
				  state= Estado.IDEN;
				  return getToken();
		
		case NUM:
			while (Character.isDigit(next()))current +=read();
			if(!formaIden(next()))return token(TipoToken.NUM);
			else return consumeError('n');
				
		case IDEN:
			while(formaIden(next())) current+=read();
			return token(TipoToken.IDEN);
			
		case COM:
			while (!finDeLinea(next()))current +=read();
			return token(TipoToken.COMENTARIO);
				
		case EOF: 
			return null;
				
		default: state=Estado.E0;
		
		}
		return null;
	}

	private Token consumeError(char id) {
		if(id=='s'){
			consumeOp();
			return error("Unknown simbol");
		}
		boolean z_err = id=='0';
		for(char n = next(); formaIden(n); n = next()){
			if(z_err) z_err= Character.isDigit(n);
			current+=read();
		}
		if(id=='_')return error("Identifiers can not begin by _");
		if(id=='0'||id=='n')return error(z_err?"Numbers can not begin by 0":"Identifiers can not begin by a number");
		return error("Unable to parse word");
	}

	private void consumeOp() {
		while(true){
			char n=next();
			if(Character.isWhitespace(n)||formaIden(n)
					||n=='\0'||n=='#'||n=='?'|n==':')break;
			read();
		}
	}

	private static boolean finDeLinea(char c){
		return c=='\n' || c=='\r' | c=='\0';
	}
	
	private static boolean formaIden(char c){
		if(Character.isWhitespace(c)|| c=='\0')return false;
		return Character.isJavaIdentifierPart(c);
	}
	
	private Token token(TipoToken tipe){
		if(tipe==TipoToken.EOF)state = Estado.EOF;
		else state = Estado.E0;
		return new Token(current, line, col, tipe);
	}
	
	private Token error(String message){
		state = Estado.E0;
		return Token.error(current, line, col, message);
	}
	
	public static void main(String[]args){
			javax.swing.JFileChooser jfc = new javax.swing.JFileChooser("../pruebas");
			jfc.showDialog(new javax.swing.JFrame(), "Tokenize");
			if(jfc.getSelectedFile()==null)System.exit(0);
			String path= jfc.getSelectedFile().getAbsolutePath();
			System.out.println(path);
			try {
				for(Token token: tokenizeFile(path))
					System.out.println(token);
			} catch (FileNotFoundException e) {}
			System.exit(0);
		}
}
