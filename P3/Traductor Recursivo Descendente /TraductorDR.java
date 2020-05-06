public class TraductorDR{

	private AnalizadorLexico al;
    private Token t;
    private StringBuilder numR;
    private TablaSimbolos ts;

    //Contructor de la clase TraductorDR
    public TraductorDR(AnalizadorLexico al){
        this.al = al;
        this.numR = new StringBuilder();
        this.t = new Token();
        t = al.siguienteToken();
        ts = null;
    }

//---------------------------------->REGLAS<----------------------------------
    // En cada una de esta regla vamos anotando los numeros de regla
    // y ejecutando los siguientes pasos de la regla para hacer la traduccion
    
    // Regla 1
    public final String S(String th){

    	String idlexema, mtrad;
    	ts = new TablaSimbolos(ts);

        if(t.tipo == Token.CLASS){
            add(1); 
            emparejar(Token.CLASS);
            idlexema = t.lexema;
            if(ts.buscar(idlexema) == null){ts.anyadir(new Simbolo(idlexema,Simbolo.CLASS,th + idlexema));}
            else{ errorSemantico(ERRYADECL,t); }
            emparejar(Token.ID);
            emparejar(Token.LBRA);
            mtrad = M(th + idlexema + "_");
            emparejar(Token.RBRA);

            return th + idlexema + mtrad;
        }
       	else {errorSintaxis(Token.CLASS);}
       	return "";
    }

    // Reglas 2,3,4
    public final String M(String th){
        if(t.tipo == Token.FUN){
        	String funtrad, mtrad;
            add(2);
            funtrad = Fun(th);
            mtrad = M(th);

            return funtrad + mtrad;
        }
        else if(t.tipo == Token.CLASS){
        	String strad, mtrad;
            add(3);
            strad = S(th);
            mtrad =M(th);

           return strad + mtrad;
        }
        else if(t.tipo == Token.RBRA || t.tipo == Token.PRINT || t.tipo == Token.IF || t.tipo == Token.ID || 
                t.tipo == Token.LBRA || t.tipo == Token.INT || t.tipo == Token.FLOAT){
            //Conjunto vacio
            add(4);
        	return "";
        } 
        else{errorSintaxis(Token.CLASS,Token.ID,Token.LBRA,Token.RBRA,Token.FUN,Token.INT,Token.FLOAT,Token.IF,Token.PRINT);}
    	return "";
    }

    // Regla 5
    public final String Fun(String th){

    	Stirng idlexema,atrad,mtrad,codtrad;
    	ts = new TablaSimbolos(ts);

        if(t.tipo == Token.FUN){
            add(5);
            emparejar(Token.FUN);
            idlexema = t.lexema;
            if(ts.buscar(idlexema) == null){ts.anyadir(new Simbolo(idlexema,Simbolo.FUN,th + idlexema));}
            else{ errorSemantico(ERRYADECL,t); }
            emparejar(Token.ID);
            atrad = A();
            emparejar(Token.LBRA);
            mtrad = M(th + idlexema + "_");
            codtrad = Cod(th + idlexema + "_");
            emparejar(Token.RBRA);

            return "void " + th + idlexema + "(" + atrad + ") {\n" + mtrad + codtrad +"\n}\n";

        }else{errorSintaxis(Token.FUN);}
        return "";
    }

    // Regla 6
    public final String A(){

    	String dvtrad, aptrad;

        if(t.tipo == Token.INT || t.tipo == Token.FLOAT){
            add(6);
            dvtrad = DV("");
            aptrad = Ap(th);

            return dvtrad + aptrad;
        }
        else{errorSintaxis(Token.INT,Token.FLOAT);}
        return "";
    }

    // Regla 7, 8
    public final String Ap(String th){
        if(t.tipo == Token.PYC){
            add(7);
            emparejar(Token.PYC);
            dvtrad = DV(th);
            aptrad = Ap(th);

            return ", " + dvtrad + aptrad;

        } else if(t.tipo == Token.LBRA){
            //Conjunto vacio
            add(8);

            return "";
        }
        else{errorSintaxis(Token.LBRA,Token.PYC);}
        return "";
    }

    // Regla 9
    public final String DV(String th){

    	String tipotrad, idlexema;
    	int temp;
    	ts = new TablaSimbolos(ts);

        if(t.tipo == Token.INT || t.tipo == Token.FLOAT){
            add(9);
            tipotrad = Tipo();
            idlexema = t.lexema;
            temp = (tipotrad == "int") ? Simbolo.ENTERO : Simbolo.REAL;
            Simbolo s = ts.buscar(idlexema);
            if(s == null){ ts.anyadir(new Simbolo(id, temp, th + idlexema)); }
            else if(!s.equals(new Simbolo(id, temp, th + idlexema))){new Simbolo(id, temp, th + idlexema);}
            else{ errorSemantico(ERRYADECL,t); }
            emparejar(Token.ID);

            return tipotrad + th + idlexema;
        }
        else{errorSintaxis(Token.INT, Token.FLOAT);}
        return "";
    }

    // Reglas 10, 11
    public final String Tipo(){
    	String idlexema;
        if(t.tipo == Token.INT){
            add(10);
            idlexema = t.lexema;
            emparejar(Token.INT);

            return idlexema;
        }
        else if(t.tipo == Token.FLOAT){
            add(11);
            idlexema = t.lexema;
            emparejar(Token.FLOAT);

            return idlexema;
        }
        else{errorSintaxis(Token.INT,Token.FLOAT);}
        return "";
    }

    // Regla 12
    public final String Cod(String th){

    	String itrad, codptrad;

        if( t.tipo == Token.PRINT || t.tipo == Token.IF || t.tipo == Token.LBRA || 
            t.tipo == Token.INT || t.tipo == Token.FLOAT|| t.tipo == Token.ID){
                add(12);
                itrad = I(th);
                codptrad = Codp(th);

                return itrad + codptrad;
        }
        else{errorSintaxis(Token.ID,Token.LBRA,Token.INT,Token.FLOAT,Token.IF,Token.PRINT);}
    	return "";
    }

    // Reglas 13, 14
    public final String Codp(String th){
        
    	String itrad, codptrad;

        if(t.tipo == Token.PYC){
            add(13);
            emparejar(Token.PYC);
            itrad = I(th);
            codptrad = Codp(th);

            return ";\n" + itrad + codptrad;
        }else if(t.tipo == Token.RBRA){
            //Conjunto vacio
            add(14);

            return "";
        } 
        else{errorSintaxis(Token.RBRA,Token.PYC);}
        return "";
    }

    // Reglas 15, 16, 17, 18, 21
    public final String I(String th){
        if(t.tipo == Token.INT || t.tipo == Token.FLOAT){
        	String dvtrad;

            add(15);
            dvtrad = DV(th);

            return dvtrad;
        } 
        else if(t.tipo == Token.LBRA){
        	String codtrad;

            add(16);
            emparejar(Token.LBRA);
            codtrad = Cod(th + "_");
            emparejar(Token.RBRA);

            return "{" + codtrad + "}";
        } 
        else if(t.tipo == Token.ID){
        	String idlexema, exprtrad;

            add(17);
            idlexema = t.lexema;
            Simbolo s = ts.buscar(idlexema);
            if(s == null){errorSemantico(ERRNODECL,t);}
            idlexema = s.nomtrad;
            emparejar(Token.ID);
            emparejar(Token.ASIG);
            exprtrad = Expr(th);

            return th + idlexema + " = " + exprtrad;
        }
        else if(t.tipo == Token.IF){
        	String exprtrad, itrad, iptrad;

            add(18);
            emparejar(Token.IF);
            exprtrad = Expr(th);
            emparejar(Token.DOSP);
            itrad = I(th);
            iptrad = Ip(th);

            return "if (" + exprtrad + "):\n" + itrad + iptrad;
        } 
        else if(t.tipo == Token.PRINT){
        	String exprtrad;

            add(21);
            emparejar(Token.PRINT);
            exprtrad = Expr(th);

            return "print(" + exprtrad + ")\n";
        } 
        else{errorSintaxis(Token.ID,Token.LBRA,Token.INT,Token.FLOAT,Token.IF,Token.PRINT);}
    	return "";
    }

    // Reglas 19, 20
    public final String Ip(String th){
        if(t.tipo == Token.ELSE){
        	String itrad;

            add(19);
            emparejar(Token.ELSE);
            itrad = I(th);
            emparejar(Token.FI);

            return "else\n" + itrad + "\n";
        }
        else if(t.tipo == Token.FI){
            add(20);
            emparejar(Token.FI);

            return "\n";
        }
        else{errorSintaxis(Token.ELSE,Token.FI);}
        return "";
    }

    //Regla 22
    public final String Expr(String th){
    	String etrad, exprtrad;

        if(t.tipo == Token.ID || t.tipo == Token.NUMENTERO || t.tipo == Token.NUMREAL || t.tipo == Token.PARI){
            add(22);
            etrad = E(th);
            exprtrad = Exprp(th);

            return etrad + exprtrad;
        } 
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI); }
    	return "";
    }

    //Reglas 23, 24 
    public final Stirng Exprp(String th){
        if(t.tipo == Token.OPREL){
        	String oprellexema, etrad;

            add(23);
            oprellexema = t.lexema;
            emparejar(Token.OPREL);
            etrad = E(th);

            return oprellexema + etrad;
        } 
        else if(t.tipo == Token.PYC || t.tipo == Token.RBRA || t.tipo == Token.ELSE || 
                t.tipo == Token.FI || t.tipo == Token.DOSP || t.tipo == Token.PARD){
            //Conjunto vacio
            add(24);
        	return "";
        }
        else{errorSintaxis(Token.RBRA,Token.PYC,Token.DOSP,Token.ELSE,Token.FI,Token.OPREL,Token.PARD);}
    	return "";
    }

    // Regla 25
    public final String E(String th){
    	String ttrad, eptrad;

        if(t.tipo == Token.ID || t.tipo == Token.NUMENTERO || t.tipo == Token.NUMREAL || t.tipo == Token.PARI){
            add(25);
            ttrad = T(th);
            eptrad = Ep(ttrad);

            return ttrad + eptrad;
        }
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI);}
    	return "";
    }


    // Reglas 26, 27
    public final String Ep(String th){
    	String opaslexema, ttrad, eptrad;

        if(t.tipo == Token.OPAS){
            add(26);
            opaslexema = t.lexema;
            emparejar(Token.OPAS);
            ttrad = T(th);
            eptrad = Ep(ttrad);

            return opaslexema + ttrad + eptrad;
        } 
        else if(t.tipo == Token.OPREL || t.tipo == Token.PYC || t.tipo == Token.RBRA || t.tipo == Token.ELSE || 
                t.tipo == Token.FI || t.tipo == Token.DOSP || t.tipo == Token.PARD){
            //Conjunto vacio
            add(27);

        	return "";
        }
        else{errorSintaxis(Token.RBRA,Token.PYC,Token.DOSP,Token.ELSE,Token.FI,Token.OPREL,Token.OPAS,Token.PARD);}
    	return "";
    }

    // Regla 28
    public final String T(String th){
    	String ftrad, tptrad;

        if(t.tipo == Token.ID || t.tipo == Token.NUMENTERO || t.tipo == Token.NUMREAL || t.tipo == Token.PARI){
            add(28);
            ftrad = F(th);
            tptrad = Tp(th);

            return ftrad + ttrad;
        }
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI);}
    	return "";
    }

    // Reglas 29, 30
    public final String Tp(String th){
    	String opmultlexema, ftrad, tptrad;

        if(t.tipo == Token.OPMUL){
            add(29);
            opmultlexema = t.lexema;
            emparejar(Token.OPMUL);
            ftrad = F(th);
            tptrad = Tp(th);

            return opmultlexema + ftrad + tptrad;
        }
        else if(t.tipo == Token.OPREL || t.tipo == Token.PYC || t.tipo == Token.RBRA || t.tipo == Token.ELSE || 
                t.tipo == Token.OPAS || t.tipo == Token.FI || t.tipo == Token.DOSP || t.tipo == Token.PARD){
            //Conjunto Vacio
            add(30);

        	return "\n";
        }
        else{errorSintaxis(Token.RBRA,Token.PYC,Token.DOSP,Token.ELSE,Token.FI,Token.OPREL,Token.OPAS,Token.OPMUL,Token.PARD);}
    	return "";
    }

    // Reglas 31, 32, 33, 34
    public final Strign F(String th){
        if(t.tipo == Token.ID){
        	String idlexema;
        	Simbolo s = ts.buscar(idlexema);
            if(s == null){errorSemantico(ERRNODECL,t);}
            idlexema = s.nomtrad;
            add(31);
            idlexema = t.lexema;
            emparejar(Token.ID);

            return th + idlexema;
        }
        else if(t.tipo == Token.NUMENTERO){
        	String numenterolexema;

            add(32);
            numenterolexema = t.lexema;
            emparejar(Token.NUMENTERO);

            return numenterolexema;
        }
        else if(t.tipo == Token.NUMREAL){
        	String numreallexema;

            add(33);
            numreallexema = t.lexema;
            emparejar(Token.NUMREAL);

            return numreallexema;
        }
        else if(t.tipo == Token.PARI){
        	String exprtrad;

            add(34);
            emparejar(Token.PARI);
            exprtrad = Expr(th);
            emparejar(Token.PARD);

            return "{" + exprtrad + "}";
        }
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI);}
    	return "";
    }


//---------------------------------->FUNCIONES ADICIONALES<----------------------------------
    
    // Añade un nuevo numero a la lista de las reglas
    private void add(int num) {numR.append(" "); numR.append(num);}
    
    // Si coinciden los dos Tokens se enparejan, si no se lanza un error
    public final void emparejar (int token){
        if(t.tipo == token){t = al.siguienteToken();}
        else{errorSintaxis(token);}
    }
    
    // Si hemos acabado de leer el ficher de forma correcta nos imprime por pantalla
    // la lista de reglas que hemos obtenido, si no lanza una excepcion indicando
    // que no se a encontrado el final del fichero
    public void comprobarFinFichero(){
        if(t.tipo != Token.EOF){errorSintaxis(Token.EOF);}
        if(f){System.out.print(numR.toString());}
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

    private final int ERRYADECL=1,ERRNODECL=2,ERRTIPOS=3,ERRNOSIMPLE=4,ERRNOENTERO=5;
  	private void errorSemantico(int nerror,Token tok){
	    System.err.print("Error semantico ("+tok.fila+","+tok.columna+"): en '"+tok.lexema+"', ");
	    switch (nerror) {
	      case ERRYADECL: System.err.println("ya existe en este ambito");
	         break;
	      case ERRNODECL: System.err.println("no ha sido declarado");
	         break;
	      case ERRTIPOS: System.err.println("tipos incompatibles entero/real");
	         break;
	      case ERRNOSIMPLE: System.err.println("debe ser de tipo entero o real");
	         break;
	      case ERRNOENTERO: System.err.println("debe ser de tipo entero");
	         break;
    	}
    	System.exit(-1);
  	}
}