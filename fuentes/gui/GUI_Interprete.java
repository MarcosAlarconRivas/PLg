package gui;

import interprete.ErrorEjecucion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import codigoP.Instruccion;
/**
 * Interfaz gráfica del interprete.
 */
public class GUI_Interprete {
	
	private enum Estado{idle, cargado, normal, traza};
	/* idle => Inicio de la aplicación, no hay ningún programa cargado
	 * cargado => Se ha cargado un programa y es posible ejecutarlo.
	 * normal => Se está ejecutando un programa en modo normal.
	 * traza => Se está ejecutando un programa en modo traza.
	 */

	private JFrame guiInterprete;
	private JTable tablaBytecode;
	private DefaultTableModel modelBytecode;
	private Consola consola;
	private GraficVM vm;
	private File ficheroByte;
	private JMenuItem mntmAbrir;
	private JLabel lblEstado;
	private JButton btnAbrir, btnNormal;	
	private Estado estado;
	private Color col_pc/*=new Color(184, 207, 229)*/;
	private int velocidad;
	private GUI_Traductor parent;

	
	public GUI_Interprete(List<Instruccion> prog, GUI_Traductor parent) {
		consola=Consola.getConsola();
		//consola.setUserInputEnabled(true);
		initialize();
		transita(Estado.idle);
		this.parent = parent;
		if(prog!=null){
			vm.load(prog);
			pintarPrograma(prog);
			transita(Estado.cargado);
		}
		if(parent!=null)
			guiInterprete.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		else 
			guiInterprete.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public GUI_Interprete(File file, GUI_Traductor parent) {
		if (file != null){
			ficheroByte = file.getAbsoluteFile();
			consola=Consola.getConsola();
			initialize();
			transita(Estado.idle);
			this.parent = parent;
			try {
				if(cargarFichero())
					transita(Estado.cargado);				
			} catch (IOException e) {
				consola.writeErr("[Error GUI]: "+e.toString());
			}
		}
		if(parent!=null)
			guiInterprete.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		else 
			guiInterprete.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void setVisible(){
		guiInterprete.setVisible(true);
	}
	
	private void initialize() {
		/*  
		 * Inicializar y configurar la ventana de la aplicación (JFrame).
		 */
		guiInterprete = new JFrame();
		guiInterprete.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if (parent != null)
					parent.restaurarConsola();
			}
		});
		guiInterprete.setResizable(false);
		guiInterprete.setIconImage(Toolkit.getDefaultToolkit().getImage(GUI_Interprete.class.getResource("/gui/resources/exec-16.png")));
		guiInterprete.setTitle("PLG Int\u00E9rprete");
		// Coloca la ventana centrada en la pantalla del usuario con un tamaño 1024 x 768 px
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		guiInterprete.setBounds(center.x-1010/2, center.y-740/2, 1010,740);

		initMenu();
		initBotones();		
		
		/*
		 *  Inicializar y configurar el panel con la consola
		 *  Contiene una tabbedPane con una pestaña "Consola"
		 */
		JTabbedPane tabbedConsola = new JTabbedPane(JTabbedPane.TOP);
		tabbedConsola.setPreferredSize(new Dimension(5, 200));
		tabbedConsola.setMaximumSize(new Dimension(32767, 100));
		tabbedConsola.addTab("Consola",consola);
		guiInterprete.getContentPane().add(tabbedConsola, BorderLayout.SOUTH);
		
		/*
		 * Inicializar y configurar el área donde se muestra el bytecode
		 * Usa también una tabbedPane con una tabla dentro. 
		 */
		JTabbedPane tabbedBytecode = new JTabbedPane(JTabbedPane.TOP);
		tabbedBytecode.setSize(300, 0);
		
		guiInterprete.getContentPane().add(tabbedBytecode, BorderLayout.WEST);
		modelBytecode = new DefaultTableModel(new String[]{"#","Bytecode","mnemotécnico"},0);
		tablaBytecode = new JTable(modelBytecode);
		tablaBytecode.setEnabled(false);
		tablaBytecode.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tablaBytecode.setFillsViewportHeight(true);
		tablaBytecode.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		col_pc=tablaBytecode.getSelectionBackground();
		JScrollPane scrollPane = new JScrollPane(tablaBytecode);
		scrollPane.setPreferredSize(new Dimension(450, 400));
		scrollPane.setMaximumSize(new Dimension(500, 400));
		tabbedBytecode.addTab("Bytecode", scrollPane);
		
		/* Inicializar y configurar el estado de la máquina virtual */
	
		JTabbedPane EstadoVM = new JTabbedPane(JTabbedPane.TOP);
		guiInterprete.getContentPane().add(EstadoVM, BorderLayout.EAST);		
		vm = new GraficVM();
		vm.setConsola(consola);
		vm.setPreferredSize(new Dimension(525, 400));
		vm.setMaximumSize(new Dimension(400, 400));
		EstadoVM.addTab("Estado VM",  new JScrollPane(vm));
	}
	
	/*  Inicializar y configurar la barra de menú  */
	private void initMenu(){

		JMenuBar barraMenu = new JMenuBar();
		guiInterprete.setJMenuBar(barraMenu);
		
		JMenu mnArchivo = new JMenu("Archivo");
		barraMenu.add(mnArchivo);
		
		mntmAbrir = new JMenuItem("Abrir");
		mntmAbrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		mntmAbrir.setIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Places-16.png")));
		mntmAbrir.addActionListener(new AbrirArchivo());
		mnArchivo.add(mntmAbrir);
		
		JMenuItem mntmSalir = new JMenuItem("Salir");
		mntmSalir.setIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/delete-16.png")));
		mntmSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guiInterprete.dispose();
			}
		});
		
		JSeparator separator_2 = new JSeparator();
		mnArchivo.add(separator_2);
		mnArchivo.add(mntmSalir);
		
		
		JMenu mnNewMenu = new JMenu("Ejecutar");
		barraMenu.add(mnNewMenu);
		
		JMenuItem mntmModoNormal = new JMenuItem("Modo normal");
		mntmModoNormal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		mntmModoNormal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modoNormal();
			}
		});
		mntmModoNormal.setIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Play-16.png")));
		mnNewMenu.add(mntmModoNormal);
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Modo traza");
		mntmNewMenuItem_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modoTraza();
			}
		});
		mntmNewMenuItem_1.setIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Step16.png")));
		mnNewMenu.add(mntmNewMenuItem_1);
		
		JSeparator separator_1 = new JSeparator();
		mnNewMenu.add(separator_1);
		
		JMenuItem mntmReiniciar = new JMenuItem("Reiniciar");
		mntmReiniciar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		mntmReiniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reinicioCompleto();
			}
		});
		mntmReiniciar.setIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Refresh-16.png")));
		mnNewMenu.add(mntmReiniciar);
		
		JMenu mnAyuda = new JMenu("Ayuda");
		barraMenu.add(mnAyuda);
		
		JMenuItem mntmACerca = new JMenuItem("Acerca de...");
		mntmACerca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		mntmACerca.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Consola.about();				
			}
		});
		mnAyuda.add(mntmACerca);
		
	}
	
	/* Inicializar y configurar la barra de botones  */	
	private void initBotones(){
		JToolBar barraBotones = new JToolBar();
		barraBotones.setEnabled(false);
		barraBotones.setFloatable(false);
		guiInterprete.getContentPane().add(barraBotones, BorderLayout.NORTH);
		
		btnAbrir = new JButton("");
		btnAbrir.addActionListener(new AbrirArchivo());
		btnAbrir.setToolTipText("Abrir...");
		
		btnAbrir.setSelectedIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Places-32s.png")));
		btnAbrir.setIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Places-32.png")));
		barraBotones.add(btnAbrir);
		
		btnNormal = new JButton("");
		btnNormal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modoNormal();
			}
		});
		btnNormal.setToolTipText("Modo normal");
		btnNormal.setSelectedIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Play-32s.png")));
		btnNormal.setIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Play-32.png")));
		barraBotones.add(btnNormal);
		
		JButton btnTraza = new JButton("");
		btnTraza.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modoTraza();}
		});
		btnTraza.setToolTipText("Modo traza");
		btnTraza.setSelectedIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Step-32s.png")));
		btnTraza.setIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Step-32.png")));
		barraBotones.add(btnTraza);
		
		JButton btnReload = new JButton("");
		btnReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reinicioCompleto();
			}
		});
		btnReload.setToolTipText("Reiniciar");
		btnReload.setIcon(new ImageIcon(GUI_Interprete.class.getResource("/gui/resources/Refresh-32.png")));
		barraBotones.add(btnReload);
		
		JSeparator separator = new JSeparator();
		separator.setMaximumSize(new Dimension(100, 33));
		separator.setSize(new Dimension(10, 10));
		separator.setPreferredSize(new Dimension(10, 10));
		separator.setOrientation(SwingConstants.VERTICAL);
		barraBotones.add(separator);
		
		JLabel labelNEstado = new JLabel("Estado -   ");
		labelNEstado.setPreferredSize(new Dimension(50, 14));
		barraBotones.add(labelNEstado);
		
		lblEstado = new JLabel();
		barraBotones.add(lblEstado);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setMaximumSize(new Dimension(100, 0));
		barraBotones.add(horizontalStrut);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setMaximumSize(new Dimension(100, 33));
		separator_1.setPreferredSize(new Dimension(10, 33));
		barraBotones.add(separator_1);
		
		JLabel lblVelocidad = new JLabel("Velocidad");
		barraBotones.add(lblVelocidad);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		barraBotones.add(horizontalStrut_1);
		
		final JSlider slider = new JSlider();
		slider.setSnapToTicks(true);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setMaximum(1000);
		slider.setValue(velocidad=0);
		slider.setMaximumSize(new Dimension(200, 50));		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put( new Integer(0), new JLabel("0 s") );
		labelTable.put( new Integer(500), new JLabel("0.5 s") );
		labelTable.put( new Integer(1000), new JLabel("1 s ") );
		slider.setLabelTable( labelTable );
		slider.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
					velocidad = slider.getValue();
			}
		});
		barraBotones.add(slider);
		
	}
	
	// Borrar la representación de la arquitectura
	private void reiniciarVM(){
		vm.restart();
		tablaBytecode.clearSelection();
		tablaBytecode.setSelectionBackground(col_pc);
	}
	
	// Limpiar toda la interfaz al estado inicial
	private void reinicioCompleto(){
		if (estado == Estado.normal){
			consola.writeWar("[Info GUI]: No es posible reiniciar cuando hay un programa en ejecución.\n");
		}
		else {
			reiniciarVM();
			modelBytecode = new DefaultTableModel(new String[]{"#","Bytecode","mnemotécnico"},0);
			tablaBytecode.setModel(modelBytecode);
			ficheroByte = null;			
			transita(Estado.idle);
		}
	}
	
	private void pintarPrograma(List<Instruccion> prog) {
		for(int i=modelBytecode.getRowCount();i>0;i--)
			modelBytecode.removeRow(i-1);
		int pc=0;
		Object fila[]= new Object[3];
		for(Instruccion ins:prog){
			fila[0]=pc++;
			fila[1]=writeAsHex(ins.toByteCode());
			fila[2]=ins.toString();
			modelBytecode.addRow(fila);
		}
	}

	private String writeAsHex(byte[] byteCode) {
		String s="";
		for(byte b:byteCode){
			String h=Integer.toHexString(b);
			int lng=h.length();
			if(lng>2){
				char dgt[]=h.toCharArray();
				h=""+dgt[lng-2]+dgt[lng-1];
			}
			s+=(h.length()<2?0:"")+h;
		}
		return s;
	}
	
	private void transita(Estado s){
		btnNormal.setSelected(s==Estado.normal);
		estado=s;
		lblEstado.setText(estado.toString());
		//btnNormal.repaint();
	}
	
	/*
	 *  Métodos de acción de los botones de la GUI
	 */

	private void modoNormal(){
		if(estado!= Estado.cargado&&estado!=Estado.traza){
			if (estado== Estado.idle) consola.writeWar("[Info GUI]: No hay ningún programa cargado.\n");
			else consola.writeWar("[Info GUI]: Ya se está ejecutando el programa.\n");
			return;
		}
		if (ficheroByte != null&&estado!=Estado.traza)
			try {
				vm.load(codigoP.ByteCode.asList(new FileInputStream(ficheroByte)));
			} catch (FileNotFoundException e1) {
				consola.writeErr(e1.toString());
				return;
			}
		
		if(estado==Estado.cargado)reiniciarVM();
		
		transita(Estado.normal);

		Thread interpreteThread = new Thread() {
		      public void run() {
		    	  while(!vm.stop()){
		    		  try {
		    			 vm.exe();
		    			
		    			 tablaBytecode.changeSelection(vm.pc(), -1, false, false);
		    			 if(velocidad>50){
		    				 tablaBytecode.repaint();
		    				 vm.repaint();
	    			 	}
		    			 Thread.sleep(velocidad);
		    		  }catch (InterruptedException e) {
		    			  tablaBytecode.changeSelection(vm.pc(), -1, false, false);
		    		  }catch(interprete.ErrorEjecucion x){
		    			  tablaBytecode.changeSelection(vm.pc(), -1, false, false);
		    			  tablaBytecode.setSelectionBackground(Color.RED);
		    		  }
		    	  }
		    	  transita(Estado.cargado);
	    		  tablaBytecode.changeSelection(vm.pc(), -1, false, false);
    			  tablaBytecode.repaint();
    			  vm.repaint();
		      }  
		    };
		    interpreteThread.start();
	}
	
	private void modoTraza(){
		if (estado == Estado.cargado) {			
			transita(Estado.traza);
			reiniciarVM();
			tablaBytecode.changeSelection(0, -1, false, false);
			//System.out.println("[Info GUI]: Iniciando maquina virtual.\n");
		}
		else if (estado == Estado.traza){
			try {
				vm.exe();
			} catch (ErrorEjecucion e) {
				tablaBytecode.setSelectionBackground(Color.RED);
			}
			if(vm.stop()){
				//System.out.println("[Info GUI]: terminado");
				transita(Estado.cargado);
			}
			tablaBytecode.changeSelection(vm.pc(), -1, false, false);
		}else{
			if (estado == Estado.idle) consola.writeWar("[Info GUI]: No hay ningún programa cargado.\n");
			else consola.writeWar("[Info GUI]: Ya se está ejecutando el programa.\n");
		}		
	}
	
	/*
	 * Abrir un fichero usando JFileChooser
	 * El resultado se muestra por la consola.
	 */
	private class AbrirArchivo implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			Object aux = e.getSource(); 
			if ((aux == btnAbrir) || (aux == mntmAbrir)){
				btnAbrir.setSelected(true);
				final JFileChooser fc = new JFileChooser("../pruebas/bin");
				int ope = fc.showOpenDialog(guiInterprete);
				btnAbrir.setSelected(false);
				if (ope == JFileChooser.APPROVE_OPTION){
					ficheroByte = fc.getSelectedFile();
					consola.write("[Info GUI]: Abriendo el fichero: "+ficheroByte.getPath()+".\n");
					try {
						if (cargarFichero()){
							consola.write("[Info GUI]: Fichero cargado correctamente.\n");
							transita(Estado.cargado);						
						}
						else
							consola.writeErr("[Error GUI]: No hay fichero de entrada.\n");
					} catch (IOException e1) {
						consola.writeErr("[Error GUI]: " + e1.toString()+"\n");
					}
				}
				else if (ope == JFileChooser.CANCEL_OPTION){
					consola.write("[Info GUI]: Cancelado por el usuario.\n");
				}
				else consola.writeErr("[Error GUI]: Error al seleccionar el fichero.\n");
			}			
		}
		
	}
	
	/*
	 * Carga de la ruta especificada en ficheroByte el fichero binario con 
	 * el código-P en la tabla hasta que llegue a final de fichero.
	 * 
	 * Esta carga no realiza ningún tipo de comprobación. Será el intérprete
	 * el que se encargue de realizarlas. Es decir, esto NO es el análisis léxico
	 */
	private boolean cargarFichero() throws IOException{
		if(ficheroByte==null||!ficheroByte.exists())return false;
		List<Instruccion> prog=codigoP.ByteCode.asList(new java.io.FileInputStream(ficheroByte));
		vm.load(prog);
		pintarPrograma(prog);
		return true;
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI_Interprete window = new GUI_Interprete((List<Instruccion>)null, null);
					window.setVisible();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
		
}