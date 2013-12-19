package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Stack;

import interprete.ErrorEjecucion;
import interprete.VM;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import codigoP.Instruccion;

/**
 * Interfaz que encapsula y muestra el estado de una VM
 */
public final class GraficVM extends JPanel {
	private static final long serialVersionUID = 1L;
	private VM vm=new VM();
	private Stack<int[]> tsl=new Stack<int[]>();
	void setConsola(Consola c){
		vm.out=c;
	}
	public void restart(){
		vm.restart();
		registros.reinicio();
		registros.actualiza(vm.getReg());
		DefaultTableModel modelPila = new DefaultTableModel(columnNames, 0);
		for (int i=vm.stackSize()-1; i>=0; i--) modelPila.addRow(new Object[]{i,""});
		pila.setModel(modelPila);
		pila.clearSelection();
		DefaultTableModel modelMemoria = new DefaultTableModel(columnNames, 0);
		for (int i=0; i<256; i++) modelMemoria.insertRow(i, new Object[]{i+1,""});
		memoria.setModel(modelMemoria);
		memoria.clearSelection();
		repaint();
		tsl.clear();
		tsl.add(new int[0]);
	}
	public void load(List<Instruccion> program){
		vm.load(program);
		restart();
	}
	public boolean stop(){
		return vm.stoped();
	}
	public int pc(){
		return registros.current[2];
	}
	private boolean returned(){
		if(vm.last_exec==null)return false;
		return vm.last_exec.opCode()==0x62;
	}
	private boolean called(){
		if(vm.last_exec==null)return false;
		return vm.last_exec.opCode()==0x61;
	}
	public void exe() throws ErrorEjecucion{
		int old_sp=vm.getReg()[0];
		vm.exe();
		int [] state= vm.getReg();
		registros.actualiza(state);
		actualizaPila(state[0], old_sp, state[1], vm.last_exec);
		if(returned()){//Actualizacion de parametros
			int p=tsl.pop().length;
			int[] c= tsl.peek();
			//actualizar vista
			for(int i=c.length;i<p;i++)//borrar antiguos
				memoria.getModel().setValueAt("", i, 1);
			for(int i=0;i<c.length;i++)//pintar nuevos
				memoria.getModel().setValueAt(c[i], i, 1);
			memoria.repaint();
		}else if(called()){
			int p=tsl.peek().length;
			int[] c=tsl.push(vm.getNewParams());
			//actualizar vista
			for(int i=c.length;i<p;i++)//borrar antiguos
				memoria.getModel().setValueAt("", i, 1);
			for(int i=0;i<c.length;i++)//pintar nuevos
				memoria.getModel().setValueAt(c[i], i, 1);
			memoria.repaint();
		}else focusParam();
		
		if(!vm.stoped());
			//focus
	}
	
	private void focusParam() {
		//int old=memoria.getSelectedRow();
		if(vm.last_exec.opCode()==0x11)
			memoria.changeSelection(((codigoP.Instruccion_op)vm.last_exec).operando()-1, -1, false, false);
		else
			memoria.clearSelection();
		/*if(old!=memoria.getSelectedRow())
			memoria.repaint();*/
	}
	private void actualizaPila(int sp, int old_sp, int sl, Instruccion ins) {
		switch(ins.opCode()){
		case 0x10:
		case 0x11:
		case 0x12:
		case 0x45:
		case 0x52:
		case 0x61:	pintaP(sp);
			break;
		case 0x60:	pintaP(sp); pintaP(sp-1);
			break;
		case 0x20:
		case 0x71:
		case 0x72: borraP(sp+1);
			break;
		case 0x30:
		case 0x31:
		case 0x32:
		case 0x33:
		case 0x34:
		case 0x35:
		case 0x40:
		case 0x41:
		case 0x42:
		case 0x43:
		case 0x44:
		case 0x50:
		case 0x51:	borraP(sp+1);
					pintaP(sp);
			break;//borra sl+1, pintar sp
		case 0x62:
					for(int p=old_sp; p>sp; p--)
						borraP(p);
					pintaP(sp);
			break;//desde sp_antiguo hasta sp nuevo borra, pinta sp
		}
		pila.changeSelection(vm.stackSize()-sp-1, -1, false, false);
		//pila.changeSelection(vm.stackSize()-(sl>=0?sl:0)-1, -1, true, true);
		pila.repaint();
		
	}
	private void pintaP(int pos){
		pila.getModel().setValueAt(vm.getFormStack(pos), vm.stackSize()-pos-1, 1);
	}
	private void borraP(int pos){
		pila.getModel().setValueAt("", vm.stackSize()-pos-1, 1);
	}

	private JTable memoria, pila;
	//private DefaultTableModel modelMemoria, modelPila; // Modelos de la memoria y la pila
	private JPanel panelMemReg; // Contiene la memoria y los registros
	private Registros registros;
	private String[] columnNames;


	public GraficVM() {
		
		// Configuración del JPanel
		setPreferredSize(new Dimension(525, 400));		
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		/*
		 * El diseño es el siguiente usando un FlowLayout
		 * - la mitad izquierda del JPanel se compone de otro JPanel con BorderLayout:
		 * 		1- Memoria en la parte superior colocada en el NORTH.
		 * 		2- Los registros en la parte inferior colocada en el CENTER.
		 * - la mitad derecha contiene directamente la tabla de la pila.
		 * 
		 * | P | S |
		 * | R | S | 		
		 *
		 * 
		 */
		//añade base de la pila
		tsl.add(new int[0]);

		// Un nuevo JPanel para la memoria y los registros.
		panelMemReg = new JPanel();
		panelMemReg.setPreferredSize(new Dimension(240, 390));	
		panelMemReg.setLayout(new BorderLayout(0, 0));	
		
		// Cabeceras de la memoria y la pila.
		columnNames = new String[]{"#", "Valor"};
		
		/* Registros */
		registros = new Registros();		 
		final TitledBorder titled = BorderFactory.createTitledBorder("Registros VM");
        registros.setBorder(titled);
        panelMemReg.add(registros, BorderLayout.SOUTH);
        
		/* Memoria */
        DefaultTableModel modelMemoria = new DefaultTableModel(columnNames, 0);
		for (int i=0; i<255; i++) modelMemoria.insertRow(i, new Object[]{i+1,""});
		memoria = new JTable(modelMemoria);
		memoria.setEnabled(false);
		memoria.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		memoria.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		memoria.setSelectionBackground(Color.CYAN);
		JScrollPane scrollMemoria = new JScrollPane(memoria);		
		scrollMemoria.setPreferredSize(new Dimension(230, 250));
		// Marco con el borde y el título
		final TitledBorder tituloMemoria = BorderFactory.createTitledBorder("Parametros locales");
		scrollMemoria.setBorder(tituloMemoria);
		panelMemReg.add(scrollMemoria, BorderLayout.CENTER);
		
		add(panelMemReg);
		
		/* Pila */
		DefaultTableModel modelPila = new DefaultTableModel(columnNames, 0);
		for (int i=vm.stackSize()-1; i>=0; i--) modelPila.addRow(new Object[]{i,""});
		pila = new JTable(modelPila);
		pila.setEnabled(false);
		pila.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		pila.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pila.setSelectionBackground(Color.GREEN);
		JScrollPane scrollPila = new JScrollPane(pila);
		scrollPila.setPreferredSize(new Dimension(240, 390));	
		
		// Marco con el borde y el título
		final TitledBorder tituloPila = BorderFactory.createTitledBorder("Pila");
		scrollPila.setBorder(tituloPila);
		add(scrollPila);
		
		// Colocar el scroll al principio de la pila
		pila.scrollRectToVisible(pila.getCellRect(pila.getRowCount()-1, 0, true));
		
	}
	
	public JTable getTableMemoria() {
		return memoria;
	}

	public JTable getTablePila() {
		return pila;
	}
	
	public JTable getTableRegs() {
		return registros.tablaRegistros;
	}
	
	private class Registros extends JPanel {
		private static final long serialVersionUID = 1L;
		int [] current;
		JTable tablaRegistros;
		Registros() {
			// Configuración del JPanel
			setMaximumSize(new Dimension(400, 100));
			current=new int[]{-1,-1,-1,-1};
			// Inicialización de las columnas y el cuerpo de la tabla
			DefaultTableModel model = new DefaultTableModel(new String[][]{{"SP",""},{"SL",""}, {"PC",""}, {"Stop",""}},  
					new String[]{"Registro","Valor"});
			// Creación y configuración de la tabla
			tablaRegistros = new JTable(model);
			tablaRegistros.setEnabled(false);
			tablaRegistros.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			tablaRegistros.setShowVerticalLines(false);
			tablaRegistros.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			tablaRegistros.setSelectionBackground(Color.CYAN);
			add(tablaRegistros);
			
		}
		public void reinicio() {
			current=new int[]{-1,-1,-1,-1};
			for(int i=0;i<4;i++)
				tablaRegistros.getModel().setValueAt("", i, 1);
		}
		void actualiza(int reg[]){
			for(int i=0;i<4;i++)
				if(current[i]!=reg[i])
					actualiza(i,reg[i]);
			repaint();
		}
		private void actualiza(int fila, int dato) {
			current[fila]=dato;
			tablaRegistros.getModel().setValueAt(dato, fila, 1);
		}
	}//end of class Registros

}