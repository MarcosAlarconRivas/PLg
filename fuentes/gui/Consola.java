package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.NoSuchElementException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


/**
 * Esta clase simula un terminal de entrada/salida.
 * 
 * Los distintos modulos pueden dar informacion al usuario llamando a:
 *  'void write(String)', 'void writeErr(String)' o 'void writeWar(String).
 *
 * Mediante 'String readLn()' se le pide una entrada al ususario, se espera a que la introduzca y se devuelve.
 * ADVERTENCIA: si el Thread de la interfaz es el mismo que el que intenta leer se puede quedar bloqueado.
 * 
 * Y con 'String re_readLn()' se vuelve a leer la ultima entrada que introdujo el usuario. 
 */
@SuppressWarnings("serial")
public class Consola extends JPanel{

	private static Consola instance = null; 
	protected static JTextPane history = new JTextPane();
	protected static UserInput input = UserInput.getField();
	protected static final Color normal= Color.WHITE;
	protected static final Color user= Color.GREEN;//el color de las entradas de usuario
	protected static final Color app= Color.CYAN;//el color de las salidas del interprete
	protected static final Color warning= Color.YELLOW;
	protected static final Color error= Color.RED;
	protected static final Color background= Color.BLACK;
	protected SimpleAttributeSet NormalAtt;
	protected SimpleAttributeSet UserAtt;
	protected SimpleAttributeSet ApplicationAtt;
	protected SimpleAttributeSet WarningAtt;
	protected SimpleAttributeSet ErrorAtt;

	

	private Consola() {
		history.setBackground(background);
		history.setForeground(normal);
		input.setBackground(background);
		input.setForeground(normal);
		history.setEditable(false);
		history.setVisible(true);
		UserInput.setOutput(this);
		setUserInputEnabled(false);
		setLayout(new BorderLayout());
		add(input, BorderLayout.SOUTH);
		add(new JScrollPane(history), BorderLayout.CENTER);
		// Mantener el scroll en la parte inferior.
		DefaultCaret caret = (DefaultCaret) history.getCaret();
        caret.setUpdatePolicy(DefaultCaret. ALWAYS_UPDATE );

		NormalAtt= new SimpleAttributeSet();
		UserAtt= new SimpleAttributeSet();
		StyleConstants.setForeground((MutableAttributeSet) UserAtt, user);
		ApplicationAtt= new SimpleAttributeSet();
		StyleConstants.setForeground((MutableAttributeSet) ApplicationAtt, app);
		WarningAtt= new SimpleAttributeSet();
		StyleConstants.setForeground((MutableAttributeSet) WarningAtt, warning);
		ErrorAtt= new SimpleAttributeSet();
		StyleConstants.setForeground((MutableAttributeSet) ErrorAtt, error);
	}
	
	/**
	 *Devuelve el terminal. [Singleton]
	 */
	public static Consola getConsola(){
		if(!(instance instanceof Consola))
			instance = new Consola();
		return instance;
	}
	
	/**
	 * Escribe datos en la consola.
	 */
	public void write(String text){
		Document d= history.getDocument();
		try {
			d.insertString(d.getLength(), text, NormalAtt);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Escribe datos en ROJO en la consola.
	 */
	public void writeErr(String text){
		Document d= history.getDocument();
		try {
			d.insertString(d.getLength(), text, ErrorAtt);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Escribe datos en AMARILLO en la consola.
	 */
	public void writeWar(String text){
		Document d= history.getDocument();
		try {
			d.insertString(d.getLength(), text, WarningAtt);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Lo usa la clase UserInput para pasar sus datos.
	 */
	void writeUser(String text) {
		Document d= history.getDocument();
		try {
			d.insertString(d.getLength(), text, UserAtt);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		
	}
	
	/**
	 * Lo usa la VM para dar la salida del programa.
	 */
	public void writeApp(String text) {
		Document d= history.getDocument();
		try {
			d.insertString(d.getLength(), text, ApplicationAtt);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		
	}
	
	/**
	 * Se bloquea hasta recibir una entrada nueva.
	 * Y le recuerda periodicamente al ususario que hay threads esperando.
	 * Cuando el usuario intorduce una linea, se despierta y la devuelve.
	 */
	public String readLn(){
		boolean b =input.isEnabled();
		if(!b){
			setUserInputEnabled(true);
			repaint();
		}
		String s = input.readLn();
		setUserInputEnabled(b);
		return s;
	}
	
	/**
	 * Devuelve la ultima linea (que ya habia sido leida mediante readLn())
	 * WRNING: si alguien vuelve a llamar a readLn cambiara el resultado.
	 */
	public String re_readLn() throws NoSuchElementException{
		return input.re_readLn();
	}
	
	/**
	 * Cabia el tiempo tras el cual se recuerda 
	 * al usuario que se esta esperando una entrada.
	 * Si se mete un numero <=0 se desactiva el aviso.
	 */
	public void setPeriodo(double segundos){
		UserInput.setPeriodo(segundos);
	}
	/**
	 * Cabia el color del texto normal del terminal.
	 */
	public void setForeground(Color c){
		input.setForeground(c);
		history.setForeground(c);
	}
	
	/**
	 * Cabia el color de fondo del terminal.
	 */
	public void setBackground(Color c){
		input.setBackground(c);
		history.setBackground(c);
	}
	
	/**
	 * Activa o desactiva la entrada para el ususario.
	 * (Si esta desactivada y se hace readLn() se activa sola hasta que se intrduzca un dato).
	 */
	public void setUserInputEnabled(boolean b){
		input.setEnabled(b);
		input.setVisible(b);
	}
	
	/**
	 * Muestra un Dialog con información del proyecto.
	 */
	public static void about(){
		JOptionPane.showMessageDialog(null,
		   "Desarrollado por:\n  Marcos Alarcón Rivas", 
		   "PLG septiembre 2013", 1);
	}
	
}
