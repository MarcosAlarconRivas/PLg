package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.JTextPane;
import javax.swing.text.Document;

import codigoP.Instruccion;

/**
 * Interfaz gráfica del traductor.
 */
@SuppressWarnings("serial")
public class GUI_Traductor extends JFrame{

	private GUI_Traductor ventana;
	private Consola consola;
	private File ficheroEntrada, ficheroSalida;
	private JLabel lblSalida;
	private JTextPane codigo;
	//private TextLineNumber tln;
	private JTabbedPane tabbedConsola;
	private JButton btnAbrir;
	java.util.List<Instruccion> compilado;

	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI_Traductor window = new GUI_Traductor();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public GUI_Traductor() {
		super();
		consola = Consola.getConsola();
		//consola.setUserInputEnabled(true);
		ficheroEntrada = null;
		ficheroSalida = null;
		initialize();
		ventana = this;
	}
	
	public void restaurarConsola(){
		consola = Consola.getConsola();
		tabbedConsola.addTab("Consola",consola);
	}

	
	private void initialize() {
		/*  
		 * Inicializar y configurar la ventana de la aplicación (JFrame).
		 */

		this.setResizable(false);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(
				GUI_Traductor.class.getResource("/gui/resources/compile-16.png")));
		this.setTitle("PLG Traductor");
		// Coloca la ventana centrada en la pantalla del usuario con un tamaño 1024 x 768 px
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		this.setBounds(center.x-1010/2, center.y-740/2, 1010,740);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		initMenu();
		initBotones();		
		
		/*
		 *  Inicializar y configurar el panel con la consola
		 *  Contiene una tabbedPane con una pestaña "Consola"
		 */
		
		tabbedConsola = new JTabbedPane(JTabbedPane.TOP);
		tabbedConsola.setPreferredSize(new Dimension(5, 200));
		tabbedConsola.setMaximumSize(new Dimension(32767, 100));
		tabbedConsola.addTab("Consola",consola);
		this.getContentPane().add(tabbedConsola, BorderLayout.SOUTH);
		
		
		/*
		 * Inicializar y configurar el área donde se muestra el código
		 * Usa también una tabbedPane con una tabla dentro. 
		 */
		JTabbedPane tabbedCodigo = new JTabbedPane(JTabbedPane.TOP);
		tabbedCodigo.setSize(300, 0);
		
		this.getContentPane().add(tabbedCodigo, BorderLayout.CENTER);
		
		codigo = new JTextPane();
		JScrollPane scroll = new JScrollPane(codigo);
		TextLineNumber tln = new TextLineNumber(codigo);
		//quitar el 1 rojo: tln.setCurrentLineForeground(tln.getForeground());
		scroll.setRowHeaderView(tln);
		tabbedCodigo.addTab("Código", null, scroll , null);
		
	}
	
	/*  Inicializar y configurar la barra de menú  */
	private void initMenu(){

		JMenuBar barraMenu = new JMenuBar();
		this.setJMenuBar(barraMenu);
		
		JMenu mnArchivo = new JMenu("Archivo");
		barraMenu.add(mnArchivo);
		
		JMenuItem mntmAbrir = new JMenuItem("Abrir");
		mntmAbrir.addActionListener(new AbrirArchivo());
		mntmAbrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 
						InputEvent.CTRL_MASK));
		mntmAbrir.setIcon(new ImageIcon(GUI_Traductor.class.getResource("/gui/resources/Places-16.png")));
		mnArchivo.add(mntmAbrir);
		
		JMenuItem mntmGuardar = new JMenuItem("Guardar");
		mntmGuardar.addActionListener(new GuardarCambios());
		mntmGuardar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, 
						InputEvent.CTRL_MASK));
		mntmGuardar.setIcon(new ImageIcon(GUI_Traductor.class.getResource("/gui/resources/Save-16.png")));
		mnArchivo.add(mntmGuardar);
		
		JMenuItem mntmSalir = new JMenuItem("Salir");
		mntmSalir.setIcon(new ImageIcon(GUI_Traductor.class.getResource("/gui/resources/delete-16.png")));
		mntmSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		JMenuItem mntmFicheroDeSalida = new JMenuItem("Fichero de salida");
		mntmFicheroDeSalida.addActionListener(new PrepararSalida());
		mntmFicheroDeSalida.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
					InputEvent.CTRL_MASK));
		mntmFicheroDeSalida.setIcon(new ImageIcon(
				GUI_Traductor.class.getResource("/gui/resources/binary-16.png")));
		mnArchivo.add(mntmFicheroDeSalida);
		
		JSeparator separator_2 = new JSeparator();
		mnArchivo.add(separator_2);
		mnArchivo.add(mntmSalir);
		
		
		JMenu mnHerramientas = new JMenu("Herramientas");
		barraMenu.add(mnHerramientas);
		
		JMenuItem mntmCompilar = new JMenuItem("Compilar");
		mntmCompilar.addActionListener(new Compilar());
		mntmCompilar.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		mntmCompilar.setIcon(new ImageIcon(
				GUI_Traductor.class.getResource("/gui/resources/compile-16.png")));
		mnHerramientas.add(mntmCompilar);
		
		JMenuItem mntmEjecutar = new JMenuItem("Ejecutar");
		mntmEjecutar.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
		mntmEjecutar.setIcon(new ImageIcon(GUI_Traductor.class.getResource("/gui/resources/Play-16.png")));
		mnHerramientas.add(mntmEjecutar);
		
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
		this.getContentPane().add(barraBotones, BorderLayout.NORTH);
		
		btnAbrir = new JButton("");
		btnAbrir.addActionListener(new AbrirArchivo());
		btnAbrir.setToolTipText("Abrir...");
		
		btnAbrir.setSelectedIcon(new ImageIcon(GUI_Traductor.class.getResource("/gui/resources/Places-32s.png")));
		btnAbrir.setIcon(new ImageIcon(GUI_Traductor.class.getResource("/gui/resources/Places-32.png")));
		barraBotones.add(btnAbrir);
		
		JButton btnGuardar = new JButton("");
		btnGuardar.addActionListener(new GuardarCambios());
		btnGuardar.setToolTipText("Guardar");
		btnGuardar.setIcon(new ImageIcon(GUI_Traductor.class.getResource("/gui/resources/Save-32.png")));
		barraBotones.add(btnGuardar);
		
		JButton btnCompilar = new JButton("");
		btnCompilar.addActionListener(new Compilar());
		btnCompilar.setToolTipText("Compilar");
		btnCompilar.setSelectedIcon(new ImageIcon(
				GUI_Traductor.class.getResource("/gui/resources/compile-32.png")));
		btnCompilar.setIcon(new ImageIcon(
				GUI_Traductor.class.getResource("/gui/resources/compile-32.png")));
		barraBotones.add(btnCompilar);
		
		JButton btnEjecutar = new JButton("");
		btnEjecutar.setToolTipText("Ejecutar");
		btnEjecutar.setSelectedIcon(new ImageIcon(GUI_Traductor.class.getResource("/gui/resources/Play-32s.png")));
		btnEjecutar.setIcon(new ImageIcon(GUI_Traductor.class.getResource("/gui/resources/Play-32.png")));
		btnEjecutar.addActionListener(new Ejecutar());
		barraBotones.add(btnEjecutar);
		
		JSeparator separator = new JSeparator();
		separator.setMaximumSize(new Dimension(100, 33));
		separator.setSize(new Dimension(10, 10));
		separator.setPreferredSize(new Dimension(10, 10));
		separator.setOrientation(SwingConstants.VERTICAL);
		barraBotones.add(separator);
		
		JButton button = new JButton("");
		button.addActionListener(new PrepararSalida());
		button.setIcon(new ImageIcon(GUI_Traductor.class.getResource("/gui/resources/binary-32.png")));
		barraBotones.add(button);
		
		JLabel labelSalida = new JLabel("Fichero de salida:    ");
		labelSalida.setPreferredSize(new Dimension(50, 14));
		barraBotones.add(labelSalida);
		
		lblSalida = new JLabel();
		barraBotones.add(lblSalida);
		
	}
	
	// Compilar
	private class Compilar implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// Sólo podemos compilar si están los ficheros preparados
			if (ficheroEntrada != null){
				// El traductor se ejecuta en otro hilo.
				Thread traductorThread = new Thread(){
					public void run() {
						traductor.Traductor as= new traductor.Traductor();
						compilado=null;
						try {
							as.procesa(traductor.lexico.AnalizadorLexico.tokenizeFile(ficheroEntrada.getPath()));
						} catch (FileNotFoundException e1) {
							consola.write("[Info GUI] Salve el fichero antes de compilar.\n");
							return;
						}
						if(as.err.isEmpty()){
							consola.write("[Info GUI] Programa compilado.");
							compilado=as.cod;
							if(ficheroSalida != null){
								//salvar el codP
								try {
									codigoP.ByteCode.saveAsFile(new java.io.FileOutputStream(ficheroSalida), compilado);
									consola.write(" Guardado en "+ficheroSalida.getPath()+"\n");
								} catch (FileNotFoundException e1) {
									consola.writeErr("\n[Error GUI]: No se pudo guardar en "+ficheroSalida.getPath()+"\n");
								}
							}else consola.write(" Para guardar el codigoP selecione un fichero de salida.\n");
						}
						else
							for(traductor.ErrorCompilacion e:as.err){
								consola.writeErr(e+"\n");
							}
						if(!as.wrng.isEmpty())
							for(traductor.ErrorCompilacion e:as.wrng)
								consola.writeWar(e+"\n");
					}
				};
				traductorThread.start();					
			}
			
			else 
				consola.writeWar("[Traductor]: Selecione el fichero a compilar.\n");
		}
		
	}
	
	
	
	// Ejecutar la compilación.
	private class Ejecutar implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
				if(compilado!=null||ficheroSalida!=null){
					if(compilado==null){
						if(ficheroSalida.exists())
							try {
								compilado=codigoP.ByteCode.asList(new FileInputStream(ficheroSalida));
								consola.write("[Traductor]: Programa cargado de "+ficheroSalida.getPath()+"\n");
							} catch (FileNotFoundException e1) {
								consola.writeWar("[Traductor]: No se pudo cargar el programa de"+ficheroSalida.getPath()+"\n");
								return;
							}
					}
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							GUI_Interprete window;
							if(compilado!=null)
								window = new GUI_Interprete(compilado,ventana);
							else
								window = new GUI_Interprete(ficheroSalida,ventana);
							
							window.setVisible();
						}
					});
					return;
				}
				if(ficheroEntrada!=null)
					if(ficheroEntrada.exists())
						consola.writeWar("[Traductor]: Debes compilar antes de ejecutar\n");
					else
						consola.writeWar("[Traductor]: "+ficheroEntrada.getPath()+" no existe.\n");
				else 
					consola.writeWar("[Traductor]: Selecione el archivo a ejecutar.\n");
		}
		
	}
	
	// Abrir un fichero con el código original
	private class AbrirArchivo implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			final JFileChooser fc = new JFileChooser("../pruebas");
			btnAbrir.setSelected(true);
			int ope = fc.showOpenDialog(ventana);
			btnAbrir.setSelected(false);
			if (ope == JFileChooser.APPROVE_OPTION){
				ficheroEntrada = fc.getSelectedFile();				
				try{
					if(ficheroEntrada.exists()){
						// Recargar si es el mismo. JTextPane no lo hace.
						Document doc = codigo.getDocument();
						if (doc != null){
							doc.putProperty(Document.StreamDescriptionProperty, null);
						}
						codigo.setPage(ficheroEntrada.toURI().toURL());
						consola.write("[Info GUI]: Abriendo el fichero: "+
							ficheroEntrada.getPath()+".\n");
					}else//si no existe, intento crearlo
						if(ficheroEntrada.createNewFile()){
							consola.write("[Info GUI]: Se ha creado fichero vacio "+
									ficheroEntrada.getPath()+".\n");
							codigo.setText("");//si se crea el fichero, borro el contenido del TextPane
						}else
							consola.writeErr("[Info GUI]: El fichero no existe y no se pudo crear uno nuevo\n");
				
				}catch (Exception e1){//se ha fallado al abrir o crear el fichero
					consola.write(e1.getMessage()+"\n");//muestro el error
				}
			}else if (ope == JFileChooser.CANCEL_OPTION){
				consola.write("[Info GUI]: Cancelado por el usuario.\n");
			}else consola.writeErr("[Error GUI]: Error al seleccionar el fichero.\n");
		}			
	}
	
	// Seleccionar un fichero para la salida del código-P
	private class PrepararSalida implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final JFileChooser fc = new JFileChooser("../pruebas/bin");
			int ope = fc.showOpenDialog(ventana);
			if (ope == JFileChooser.APPROVE_OPTION){
				ficheroSalida = fc.getSelectedFile();
				lblSalida.setText(ficheroSalida.getPath());
				consola.write("[Info GUI]: Fichero de salida: "+
						ficheroSalida.getPath()+".\n");
			}
			else if (ope == JFileChooser.CANCEL_OPTION){
				consola.write("[Info GUI]: Cancelado por el usuario.\n");
			}
			else consola.writeErr("[Error GUI]: Error al seleccionar el fichero.\n");
		}			
	}
	
	private class GuardarCambios implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			try{
				if (ficheroEntrada == null){
					final JFileChooser fc = new JFileChooser("../pruebas");
					btnAbrir.setSelected(true);
					int ope = fc.showOpenDialog(ventana);
					btnAbrir.setSelected(false);
					if (ope == JFileChooser.APPROVE_OPTION)
						ficheroEntrada = fc.getSelectedFile();	
				}
				java.io.FileWriter TextOut = new java.io.FileWriter(ficheroEntrada, false);
				TextOut.write(codigo.getText());
				TextOut.close();
				consola.write("[Info GUI]: Fichero guardado.\n");

			}catch(IOException t){
				consola.writeErr("[Error GUI]: "+t.toString()+"\n");
			}
		}
		
	}

}
