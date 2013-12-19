package traductor.ts;

import java.util.HashMap;
import java.util.List;

public class TablaSimbolos extends HashMap<String, Propiedades> {
	
	private static final long serialVersionUID = 1L;
	TablaLocal local = null;

	public boolean declaraFun(String id, List<String> params, int etqh){
		if(existeFun(id))return false;
		PropFuncion prop = Propiedades.declaraFun(params);
		prop.etqh=etqh;
		return super.put(id, prop)==null;
	}
	
	public boolean existeID(String id){
		return existeFun(id)||existeParam(id);
	}
	
	/** Carga la tabla local de la funcion especificada*/
	public void call(String fun){
		if(!existeFun(fun))return;
		PropFuncion pf =(PropFuncion)super.get(fun);
		local=pf.tl;
	}
	
	/**Restaura la tabla global, ocultando la local*/
	public void return_(){
		local=null;
	}
	
	public int aridad(String fun){
		if(!existeFun(fun))return -1;
		return ((PropFuncion)super.get(fun)).numOfParam();
	}
	
	public int aridad(){
		return local.size();
	}
	
	public int etqh(String fun){
		if(!existeFun(fun))return -1;
		return ((PropFuncion)super.get(fun)).etqh;
	}
	
	public int wichParamIs(String param){
		if(local==null)return 0;
		PropParametro p=local.get(param);
		if(p==null)return 0;
		return p.pos;
	}
	
	public boolean existeFun(String id){
		return super.containsKey(id);
	}
	
	public boolean existeParam(String id){
		if(local==null)return false;
		return local.containsKey(id);
	}
	
	public boolean inMain(){
		return local==null;
	}
	
}

class TablaLocal extends HashMap<String, PropParametro>{
	private static final long serialVersionUID = 1L;

	public TablaLocal(PropFuncion fun){
		int i=1;
		for(String nombre:fun.parametros)
			super.put(nombre, Propiedades.declaraParam(i++));
	}
}

