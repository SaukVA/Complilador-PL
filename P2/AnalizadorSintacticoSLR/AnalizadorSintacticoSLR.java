
import java.util.*;

public class AnalizadorSintacticoSLR{

	private boolean flag;
	private AnalizadorLexico al;
	private Token t;
	private Stack<Integer> pila, resultado; 


	//Constructor del Analizador Sintactico Ascendente 
	public AnalizadorSintacticoSLR(AnalizadorLexico al){
		flag = true;
		this.al = al;
		token = al.siguienteToken();
		pila  = new Stack<Integer>;
		resultado = new Stack<Integer>;
	}


	/*push(0)
	a := siguienteToken()
	REPETIR
		sea s el estado en la cima de la pila
		SI Accion[s, a] = dj ENTONCES
			push(j)
			a := siguienteToken()
		SI NO SI Accion[s, a] = rk ENTONCES
			PARA i := 1 HASTA Longitud_Parte_Derecha(k) HACER pop()
			sea p el estado en la cima de la pila
			sea A la parte izquierda de la regla k
			push(Ir_A[p, A])
		SI NO SI Accion[s, a] = aceptar ENTONCES
			fin del analisis
		SI NO
			error()
		FIN_SI
	HASTA fin del analisis*/

	public void  analizar(){

		boolean salida = false;
		int estado, p;
		String a, regla; 

		pila.push(0);

		while(!salida){

			estado = getEstado();
			accion(estado, token, a, p);

			switch (a){
				case "d": //------> CASO DESPLAZAR
					pila.push(p);
					token = al.siguienteToken();
					break;
				case "r": //------> CASO REDUCCION
					for(int i=1; i<= p; i++){pila.pop();}
					p = getEstado();
					regla = getRegla(k);
					resultado.push(ir_A(p, regla));
					break;
				case "a": //------> CASO ACEPTAR
					salida = true;
					break;
				default: //-------> CASO ERROR
					printErrorres();
					break;
			}
		}

		imprimir();

	}

	//-------------------------> REGLAS <-------------------------


	//-------------------------> FUNCIONES EXTRA <-------------------------


	public int  getEstado(){
		return 1;
	}

	// Muestra los errores que se han obtenido durante la ejecucion y el porque de ellos.
    // Se controlan dos tipos de casos, el primero es aquel en el que se obtiene un final
    // de fichero inesperado, y el segundo caso es aquel en el que se obtienen otros valores
    // diferentes a los esperados
    public final void errorSintaxis(int ... args){

        StringBuilder sal = new StringBuilder();

        if(t.tipo == Token.EOF){System.err.print("Error sintactico: encontrado fin de fichero, esperaba ");}

        else{sal.append("Error sintactico (" + t.fila + "," + t.columna +"): encontrado '" + t.lexema + "', esperaba ");}
        
        for(int arg : args){
            t.tipo = arg;
            sal.append(" " + t.toString());
        }
        
        System.err.print(sal);
        System.exit(-1);
    }


}