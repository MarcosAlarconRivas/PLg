package traductor.ts;

import java.util.List;

public abstract class Propiedades {
	enum Tipo{fun, param}
	public final Tipo tipo=null;
	
	public static PropFuncion declaraFun(List<String> parametros){
		return new PropFuncion(parametros);
	}

	public static PropParametro declaraParam(int i){
		PropParametro v=new PropParametro();
		v.pos=i;
		return v;
	}

}
class PropFuncion extends Propiedades{
	public final Tipo tipo=Tipo.fun;
	public int etqh;
	public List<String> parametros;
	TablaLocal tl;
	public PropFuncion(List<String> params){
		parametros=params;
		tl=new TablaLocal(this);
	}
	public int numOfParam(){
		return parametros.size();
	}

	
}
class PropParametro extends Propiedades{
	public final Tipo tipo=Tipo.param;
	public int pos;
	
}
