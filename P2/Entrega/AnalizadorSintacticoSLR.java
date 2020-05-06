import java.util.*;

public class AnalizadorSintacticoSLR{

	private AnalizadorLexico al;
	private Token token;
	private Stack<Integer> pila, resultado;
	private String accion;
	private int clausura;


	//Constructor del Analizador Sintactico Ascendente 
	public AnalizadorSintacticoSLR(AnalizadorLexico al){
		this.al = al;
		token = al.siguienteToken();
		pila  = new Stack<>();
		resultado = new Stack<>();
		accion = "";
		clausura = 0;
	}

	// Funcion principal de la clase AnalizadorSintacticoSLR la cual se añade el valor 0 a la 
	// pila y se empieza un bucle hata obtener el resultado final (un string de las reglas aplicadas)
	// o un error de ejecución. Dentro del bucle se comprueba la acción a realizar y se ejecuta. Las 
	// acciones a realizar son 3 "desplazar", "reducir" y "acceptar". Si se desplaza se añade la 
	// clausura a la pila qe tenemos de los  elementos, si se reduce se eliminarian de la pila tantos 
	// elementos como tamaño de la regla, y si aceptamos, se muestra por pantalla las reglas que se 
	// han aplicado.
	public void  analizar(){
		boolean salir = false;
		int estado;
		String regla = "";
		
		pila.push(0);
		while(!salir){
			accion(getEstadoCima(),token);
			switch(accion){
				case "d": //----> Caso de que se obtenga un desplazamiento 
					pila.push(clausura);
					token = al.siguienteToken();
					break;
				case "r": //----> Caso de que se obtenga una redución
					regla = reducir(clausura);
					pila.push(ir_A(getEstadoCima(),regla));
					break;
				case "aceptar": //----> Caso de que se obtenga la aceptacion final
					salir = true;
					imprimirSolucion();
					break;
				default: //----> Caso en el que se obtiene un Error
					/*imprimirError(clausura);*/
					break;
			}
		}
		//imprimirSolucion();
	}

	//-----------------------------------> FUNCIONES EXTRA <-----------------------------------

	// Funcion que nos devuelve el ultimo elemento añadido a la pila 
	private int  getEstadoCima(){ return pila.lastElement(); }

	// En esta funcion se nos devuelve el tipo de accion que se va a realizar en funcion del 
	// estado actual y el token introducido. Siendo: "desplaza", "reucir" o aceptar
	private void accion(int estado, Token t){
		switch(estado){
			case 0:
				if(t.tipo == Token.CLASS){accion = "d"; clausura = 2;}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.CLASS);}  
				break;
			case 1:
				if(t.tipo == Token.EOF){accion = "aceptar"; clausura = -1;}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.EOF);} 
				break;
			case 2:
				if(t.tipo == Token.ID){accion = "d"; clausura = 3;}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.ID);}
				break;
			case 3:
				if(t.tipo == Token.LBRA){accion = "d"; clausura = 4;}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.LBRA);} 
				break;
			case 4:
			case 6:
			case 7:
			case 13:
				switch(t.tipo){
					case Token.CLASS: accion = "d"; clausura = 2; break;
					case Token.FUN: accion = "d"; clausura = 8; break;
					case Token.ID:
					case Token.LBRA:
					case Token.RBRA:
					case Token.INT:
					case Token.FLOAT:
					case Token.PRINT: accion = "r"; clausura = 4; break;
					default: /*imprimirError(estado);*/  
						errorSintaxis(Token.CLASS,Token.ID,Token.LBRA,Token.RBRA,Token.FUN,Token.INT,Token.FLOAT,Token.PRINT); 
						break;
				}
				break;
			case 5:
				if(t.tipo == Token.RBRA){accion = "d"; clausura = 9;}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.RBRA);} 
				break;
			case 8:
				if(t.tipo == Token.ID){accion = "d"; clausura = 12;}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.ID);} 
				break;
			case 9:
				switch(t.tipo){
					case Token.CLASS: 
					case Token.FUN: 
					case Token.ID:
					case Token.LBRA:
					case Token.RBRA:
					case Token.INT:
					case Token.FLOAT:
					case Token.PRINT: 
					case Token.EOF: accion = "r"; clausura = 1; break;
					default: /*imprimirError(estado);*/  
						errorSintaxis(Token.CLASS,Token.ID,Token.LBRA,Token.RBRA,Token.FUN,Token.INT,Token.FLOAT,Token.PRINT,Token.EOF); 
						break;
				}
				break;
			case 10:
			case 11:
				switch(t.tipo){
					case Token.ID:
					case Token.LBRA:
					case Token.RBRA:
					case Token.INT:
					case Token.FLOAT:
					case Token.PYC:
					case Token.PRINT: accion = "r"; 
						if(estado == 10){clausura = 2;}else{clausura = 3;}
						break;
					default: /*imprimirError(estado);*/  
						errorSintaxis(Token.ID,Token.LBRA,Token.RBRA,Token.INT,Token.FLOAT,Token.PYC,Token.PRINT); 
						break;
				}
				break;
			case 12:
				if(t.tipo == Token.LBRA){accion = "d"; clausura = 13;}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.LBRA);} 
				break;
			case 14:
			case 18:
				switch(t.tipo){
					case Token.ID: accion = "d"; clausura = 19; break;
					case Token.LBRA: accion = "d"; clausura = 18; break;
					case Token.INT: accion = "d"; clausura = 22; break;
					case Token.FLOAT: accion = "d"; clausura = 23; break;
					case Token.PRINT:  accion = "d"; clausura = 20; break;
					default: /*imprimirError(estado);*/  
						errorSintaxis(Token.ID,Token.LBRA,Token.INT,Token.FLOAT,Token.PRINT); 
						break;
				}
				break;
			case 15:
			case 16:
			case 17:
			case 26:
			case 33:
			case 34:
			case 35:
				if(t.tipo == Token.RBRA || t.tipo == Token.PYC){
					if (estado == 15 || estado == 26){accion = "d";}
					else{accion = "r";}
					switch (estado){
						case 15: clausura = (t.tipo == Token.RBRA) ? 24 : 25; break;
						case 16: clausura = 10; break;
						case 17: clausura = 11; break;
						case 26: clausura = (t.tipo == Token.RBRA) ? 35 : 25; break;
						case 33: clausura = 6; break;
						case 34: clausura = 9; break;
						case 35: clausura = 12; break;
					}
				}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.RBRA,Token.PYC);} 
				break;
			case 19:
				if(t.tipo == Token.ASIG){accion = "d"; clausura = 27;}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.ASIG);} 
				break;
			case 20:
			case 27:
			case 37:
				switch(t.tipo){
					case Token.ID: accion = "d"; clausura = 32;break;
					case Token.NUMENTERO: accion = "d"; clausura = 30;break;
					case Token.NUMREAL: accion = "d"; clausura = 31; break;
					default: /*imprimirError(estado);*/  errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL); break;
				}
				break;
			case 21:
				if(t.tipo == Token.ID){accion = "d"; clausura = 33;}
				else{/*imprimirError(estado);*/ errorSintaxis(Token.ID);} 
				break;
			case 22:
				if(t.tipo == Token.ID){accion = "r"; clausura = 7;}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.ID);} 
				break;
			case 23:
				if(t.tipo == Token.ID){accion = "r"; clausura = 8;}
				else{/*imprimirError(estado);*/  errorSintaxis(Token.ID);} 
				break;
			case 24:
				switch(t.tipo){
					case Token.CLASS: 
					case Token.FUN: 
					case Token.ID:
					case Token.LBRA:
					case Token.RBRA:
					case Token.INT:
					case Token.FLOAT:
					case Token.PYC:
					case Token.PRINT: accion = "r"; clausura = 5; break;
					default: /*imprimirError(estado);*/  
						errorSintaxis(Token.CLASS,Token.ID,Token.LBRA,Token.RBRA,Token.FUN,Token.INT,Token.FLOAT,Token.PYC,Token.PRINT); break;
				}
				break;
			case 25:
				switch(t.tipo){
					case Token.ID: accion = "d"; clausura = 19; break;
					case Token.LBRA: accion = "d"; clausura = 18; break;
					case Token.INT: accion = "d"; clausura = 22; break;
					case Token.FLOAT: accion = "d"; clausura = 23; break;
					case Token.PRINT: accion = "d"; clausura = 20; break;
					default: /*imprimirError(estado);*/  
						errorSintaxis(Token.ID,Token.LBRA,Token.INT,Token.FLOAT,Token.PRINT); break;
				}
				break;
			
			case 28:
				switch(t.tipo){
					case Token.RBRA:
					case Token.PYC: accion = "r"; clausura = 14; break;
					case Token.OPAS: accion = "d"; clausura = 37; break;
					default: /*imprimirError(estado);*/  
						errorSintaxis(Token.RBRA,Token.PYC,Token.OPAS); 
						break;
				}
				break;
			case 29:
			case 30:
			case 31:
			case 32:
				switch(t.tipo){
					case Token.RBRA:
					case Token.PYC: 
					case Token.OPAS: accion = "r";
						switch(estado){
							case 29: clausura = 16;break;
							case 30: clausura = 17;break;
							case 31: clausura = 18;break;
							case 32: clausura = 19;break;
						}
						break;
					default: /*imprimirError(estado);*/
						errorSintaxis(Token.RBRA,Token.PYC,Token.OPAS); 
						break;
				}
				break;
			case 36:
				switch(t.tipo){
					case Token.RBRA:
					case Token.PYC: accion = "r"; clausura = 13;break;
					case Token.OPAS: accion = "d"; clausura = 37;break;
					default: /*imprimirError(estado);*/  
						errorSintaxis(Token.RBRA,Token.PYC,Token.OPAS); 
						break;
				}
				break;
			case 38: 
				switch(t.tipo){
					case Token.RBRA:
					case Token.PYC: 
					case Token.OPAS: accion = "r"; clausura = 15;break;
					default: /*imprimirError(estado);*/  
						errorSintaxis(Token.RBRA,Token.PYC,Token.OPAS); 
					break;
				}
				break;
		}
	}

	// Reducira la pila un tamaño equivalente a el tamaño de la parte izquierda
	// de la regla que se va a utilizar y nos nos devuelve la regla para saber 
	// a donde vamos a apuntar en Ir_A()  
	private String reducir(int regla){
		resultado.push(regla);
		int pop = 0;
		String r = "";

		switch(regla){
			case 1: pop = 5; r = "S"; break;
			case 2: 
			case 3: pop = 2; r = "M"; break;
			case 4: r = "M"; break;
			case 5: pop = 6; r = "Fun"; break;
			case 6: pop = 2; r = "DV"; break;
			case 7: 
			case 8: pop = 1; r = "Tipo"; break;
			case 9: pop = 3; r = "Cod"; break;
			case 10: pop = 1; r = "Cod"; break;
			case 11: pop = 1; r = "I"; break;
			case 12: 
			case 13: pop = 3; r = "I"; break;
			case 14: pop = 2; r = "I"; break;
			case 15: pop = 3; r = "E"; break;
			case 16: pop = 1; r = "E"; break;
			case 17: 
			case 18: 
			case 19: pop = 1; r = "F"; break;
		}
		for(int i=1; i<=pop; i++){pila.pop();}
		return r;
	}

	// Dependiendo de la regla que se haya reducido y del valor que se haya quedado en la
	// cima se nos devuelve el valor que se va a introducir en la pila 
	private int ir_A(int cima, String regla){
		switch(regla){
			case "S": 
				switch(cima){
					case 0: return 1;
					case 4:
					case 6:
					case 7: 
					case 13: return 7;
				}
				break;
			case "M": 
				switch(cima){
					case 4: return 5;
					case 6: return 10;
					case 7: return 11;
					case 13: return 14;
				}
				break;
			case "Fun": 
				switch(cima){
					case 4:
					case 6:
					case 7: 
					case 13: return 6;
				}
				break;
			case "DV":
				switch(cima){
					case 14:
					case 18:
					case 25: return 17;
				}
				break;
			case "Tipo": 
				switch(cima){
					case 14:
					case 18:
					case 25: return 21;
				}
				break;
			case "Cod": 
				switch(cima){
					case 14: return 15;
					case 18: return 26;
				}
				break;
			case "I": 
				switch(cima){
					case 14:
					case 18: return 16;
					case 25: return 34;
				}
				break;
			case "E": 
				switch(cima){
					case 20: return 28;
					case 27: return 36;
				}
				break;
			case "F": 
				switch(cima){
					case 20: 
					case 27: return 29;
					case 37: return 38;
				}
				break;
		}
		return 70;
	}

	// Nos imprime las reglas que ha utiliza
	private final void imprimirSolucion(){
		String salida = "";
		salida += resultado.pop().toString();
		while(!resultado.empty()){salida += " " + resultado.pop().toString();}
		System.out.println(salida);
	}

	// Muestra los errores que se han obtenido durante la ejecucion y el porque de ellos.
    // Se controlan dos tipos de casos, el primero es aquel en el que se obtiene un final
    // de fichero inesperado, y el segundo caso es aquel en el que se obtienen otros valores
    // diferentes a los esperados
    private void errorSintaxis(int ... args){

        StringBuilder sal = new StringBuilder();

        if(token.tipo == Token.EOF){System.err.println("Error sintactico: encontrado fin de fichero, esperaba ");}

        else{sal.append("Error sintactico (" + token.fila + "," + token.columna +"): encontrado '" + token.lexema + "', esperaba ");}
        
        for(int arg : args){
            token.tipo = arg;
            sal.append(" " + token.toString());
        }
        
        System.err.println(sal);
        System.exit(-1);
    }

}