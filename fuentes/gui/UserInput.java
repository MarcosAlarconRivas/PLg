package gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class UserInput extends JTextField implements KeyListener{
	
	private static UserInput instance = new UserInput();
	private static Consola output = null;
	private LinkedList<String> buffer = new LinkedList<String>();
	private ListIterator<String> iterator= buffer.listIterator();
	private String current_buffer= null;
	private boolean toRead= false;
	private LinkedList<Thread> waiting= new LinkedList<Thread>();
	private static double seg = 15;
	
	private UserInput(){
		addKeyListener(this);
		setVisible(true);
		setEnabled(true);
	}
	
	static synchronized void setOutput(Consola c){
		output= c;
	}
	
	public static UserInput getField(){
		if(instance instanceof UserInput)
			instance.repaint();
		else
			rebulidInstance();
		
		return instance;
	}
	
	static void rebulidInstance(){
		instance = new UserInput();
		instance.repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char c= e.getKeyChar();
		if(c!='\r'&&c!='\n') return;
		
		String text = getText();
		if(output==null){
			JOptionPane.showMessageDialog(null,
				"input recived:\n\t"+text, 
				"Console info", 1);
		}else{
				output.write("[User input]: ");
				output.writeUser(text);
				if(waiting.isEmpty())
					output.write("\n");
		}
		if(text.length()>0)//si la cadena no es vacia
			synchronized(this){
				buffer.addFirst(getText());
				toRead= true;
				setText("");
				iterator= buffer.listIterator();
				for(Thread th: waiting)	th.interrupt();
				//despertar a todo el que este esperando una entrada
			}
		current_buffer= null;
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
	    int keyCode = e.getKeyCode();
	    switch( keyCode ) {
	        case KeyEvent.VK_UP:
	        	if(iterator.hasNext()){
	        		if(!iterator.hasPrevious())
	        			current_buffer= getText();
	            	setText(iterator.next());
	        	}
	            break;
	        case KeyEvent.VK_DOWN:
	        	if(iterator.hasPrevious())
	            	setText(iterator.previous());
	        	else
	        		setText(current_buffer);
	            break;
	     }
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	
	static void setPeriodo(double d){
		seg=d;
	}
	
	/**
	 * Se bloquea hasta recibir una entrada nueva.
	 * Cada 'seg' segundos le recuerda al usuario que se esta esperando.
	 * Cuando el usuario intorduce una linea, se despierta y la devuelve.
	 */
	public String readLn() {
		synchronized(this){
			waiting.add(Thread.currentThread());
		}
		while(!toRead)try{
			Thread.sleep(Math.round(1000*(seg>0?seg:3600)));
			if(seg>0 && output!=null)
				output.write("[Runtime Info]: "+Thread.currentThread().getName()+" ["+(waiting.indexOf(Thread.currentThread())+1)+"/"+waiting.size()+"] is waiting for user input.\n");
		}catch(InterruptedException e){
			break;
		}
		synchronized(this){
			waiting.remove(Thread.currentThread());
			if(waiting.isEmpty()) toRead= false;
			return buffer.getFirst();
		}
	}
	
	/**
	 * Devuelve la ultima linea (que ya habia sido leida mediante readLn())
	 * WRNING: si alguien vuelve a llamar a readLn cambiara el resultado.
	 */
	public synchronized String re_readLn() throws NoSuchElementException{
		return buffer.getFirst();
	}
	
}
