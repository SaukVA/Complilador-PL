public class TraductorDR{

	private AnalizadorLexico al;
    private Token t;
    private StringBuilder numR;

    //Contructor de la clase TraductorDR
    public TraductorDR(AnalizadorLexico al){
        this.al = al;
        this.numR = new StringBuilder();
        this.t = new Token();
        t = al.siguienteToken();
    }

//---------------------------------->REGLAS<----------------------------------
    // En cada una de esta regla vamos anotando los numeros de regla
    // y ejecutando los siguientes pasos de la regla para hacer la traduccion
    
    // Regla 1
    public final String S(String th){

    	String idlexema, mtrad;
        TablaSimbolos ts = null;
        ts = new TablaSimbolos(null);

        if(t.tipo == Token.CLASS){
            add(1); 
            emparejar(Token.CLASS);
            idlexema = t.lexema;
            if(ts.buscar(idlexema) == null){ts.anyadir(new Simbolo(idlexema,Simbolo.CLASS,th + idlexema));}
            else{ errorSemantico(ERRYADECL,t); }
            emparejar(Token.ID);
            emparejar(Token.LBRA);
            mtrad = M(th + idlexema + "_", ts);
            emparejar(Token.RBRA);

            return "// class " + th + idlexema + "\n" + mtrad + "\n";
        }
       	else {errorSintaxis(Token.CLASS);}
       	return "";
    }

    // Reglas 2,3,4
    public final String M(String th, TablaSimbolos ts){
        if(t.tipo == Token.FUN){
        	String funtrad, mtrad;
            add(2);
            funtrad = Fun(th, ts);
            mtrad = M(th, ts);

            return funtrad + mtrad;
        }
        else if(t.tipo == Token.CLASS){
        	String strad, mtrad;
            add(3);
            strad = S(th);
            mtrad =M(th, ts);

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
    public final String Fun(String th, TablaSimbolos ts){

    	String idlexema,atrad,mtrad,codtrad;
        TablaSimbolos ts2;
    	ts2 = new TablaSimbolos(ts);

        if(t.tipo == Token.FUN){
            add(5);
            emparejar(Token.FUN);
            idlexema = t.lexema;
            Simbolo s = ts2.buscar(idlexema);
            if(s == null || s.tipo != Simbolo.FUN){ts2.anyadir(new Simbolo(idlexema,Simbolo.FUN,th + idlexema)); ts2 = new TablaSimbolos(ts2);}
            else{ errorSemantico(ERRYADECL,t); }
            emparejar(Token.ID);
            atrad = A(ts2);
            emparejar(Token.LBRA);
            mtrad = M(th + idlexema + "_", ts2);
            codtrad = Cod(th + idlexema + "_", ts2);
            emparejar(Token.RBRA);

            return "void " + th + idlexema + "(" + atrad + ") {\n" + mtrad + codtrad +"\n} // " + th + idlexema + "\n\n";

        }else{errorSintaxis(Token.FUN);}
        return "";
    }

    // Regla 6
    public final String A(TablaSimbolos ts){

    	String dvtrad, aptrad;

        if(t.tipo == Token.INT || t.tipo == Token.FLOAT){
            add(6);
            dvtrad = DV("", ts);
            aptrad = Ap(ts);

            return dvtrad + aptrad;
        }
        else{errorSintaxis(Token.INT,Token.FLOAT);}
        return "";
    }

    // Regla 7, 8
    public final String Ap(TablaSimbolos ts){
        String dvtrad, aptrad;

        if(t.tipo == Token.PYC){
            add(7);
            emparejar(Token.PYC);
            dvtrad = DV("", ts);
            aptrad = Ap(ts);

            return "," + dvtrad + aptrad;

        } else if(t.tipo == Token.LBRA){
            //Conjunto vacio
            add(8);

            return "";
        }
        else{errorSintaxis(Token.LBRA,Token.PYC);}
        return "";
    }

    // Regla 9
    public final String DV(String th, TablaSimbolos ts){

    	String tipotrad, idlexema;
    	int temp;
        String ultimo = "";

        if(t.tipo == Token.INT || t.tipo == Token.FLOAT){
            add(9);
            tipotrad = Tipo(ts);
            idlexema = t.lexema;
            if (tipotrad == "int "){temp = Simbolo.ENTERO;}
            else{temp = Simbolo.REAL;}
            Simbolo s = ts.buscar(idlexema);
            Simbolo padre = ts.getPadre().buscar(idlexema);
            if(s == null){ ts.anyadir(new Simbolo(idlexema, temp, th + idlexema)); }
            else if(s.tipo != temp){ts.anyadir(new Simbolo(idlexema, temp, th + idlexema));}
            else if(padre != null){ts.anyadir(new Simbolo(idlexema, temp, th + idlexema));}
            else{ errorSemantico(ERRYADECL,t); }
            emparejar(Token.ID);

            return tipotrad + th + idlexema;
        }
        else{errorSintaxis(Token.INT, Token.FLOAT);}
        return "";
    }

    // Reglas 10, 11
    public final String Tipo(TablaSimbolos ts){
    	String idlexema;
        if(t.tipo == Token.INT){
            add(10);
            idlexema = t.lexema;
            emparejar(Token.INT);

            return "int ";
        }
        else if(t.tipo == Token.FLOAT){
            add(11);
            idlexema = t.lexema;
            emparejar(Token.FLOAT);

            return "float ";
        }
        else{errorSintaxis(Token.INT,Token.FLOAT);}
        return "";
    }

    // Regla 12
    public final String Cod(String th, TablaSimbolos ts){

    	String itrad, codptrad;

        if( t.tipo == Token.PRINT || t.tipo == Token.IF || t.tipo == Token.LBRA || 
            t.tipo == Token.INT || t.tipo == Token.FLOAT|| t.tipo == Token.ID){
                add(12);
                itrad = I(th, ts);
                codptrad = Codp(th, ts);

                return itrad + codptrad;
        }
        else{errorSintaxis(Token.ID,Token.LBRA,Token.INT,Token.FLOAT,Token.IF,Token.PRINT);}
    	return "";
    }

    // Reglas 13, 14
    public final String Codp(String th, TablaSimbolos ts){
        
    	String itrad, codptrad;

        if(t.tipo == Token.PYC){
            add(13);
            emparejar(Token.PYC);
            itrad = I(th, ts);
            codptrad = Codp(th, ts);

            return "\n\n" + itrad + codptrad;
        }else if(t.tipo == Token.RBRA){
            //Conjunto vacio
            add(14);

            return "";
        } 
        else{errorSintaxis(Token.RBRA,Token.PYC);}
        return "";
    }

    // Reglas 15, 16, 17, 18, 21
    public final String I(String th, TablaSimbolos ts){
        if(t.tipo == Token.INT || t.tipo == Token.FLOAT){
        	String dvtrad;

            add(15);
            dvtrad = DV(th, ts);

            return dvtrad + ";";
        } 
        else if(t.tipo == Token.LBRA){
            TablaSimbolos ts2;
            ts2 = new TablaSimbolos(ts);
        	String codtrad;

            add(16);
            emparejar(Token.LBRA);
            codtrad = Cod(th + "_", ts2);
            emparejar(Token.RBRA);

            return "{\n" + codtrad + "\n\n}\n";
        } 
        else if(t.tipo == Token.ID){
        	String idlexema;
            Simbolo exprtrad;
            Token temp = new Token();

            add(17);
            idlexema = t.lexema;
            Simbolo s = ts.buscar(idlexema);
            if(s == null){errorSemantico(ERRNODECL,t);}
            if(s.tipo != Simbolo.ENTERO && s.tipo != Simbolo.REAL){errorSemantico(ERRNOSIMPLE,t);}
            idlexema = s.nomtrad;
            emparejar(Token.ID);
            temp.tipo = t.tipo;
            temp.fila = t.fila;
            temp.columna = t.columna;
            temp.lexema = t.lexema;
            emparejar(Token.ASIG);
            exprtrad = Expr(th, ts);

            if(s.tipo == Simbolo.ENTERO){
                if(exprtrad.tipo == Simbolo.ENTERO){
                    return "  " + idlexema + " = " + exprtrad.nombre + ";";
                }
                else{errorSemantico(ERRTIPOS,temp);}
            }
            else{
                 if(exprtrad.tipo == Simbolo.ENTERO){
                    return "  " + idlexema + " = itor(" + exprtrad.nombre + ");";
                }
                else{
                    return "  " + idlexema + " = " + exprtrad.nombre + ";";
                }
            }
        }
        else if(t.tipo == Token.IF){
        	String itrad, iptrad;
            Simbolo exprtrad;

            add(18);
            emparejar(Token.IF);
            exprtrad = Expr(th, ts);
            emparejar(Token.DOSP);
            itrad = I(th, ts);
            iptrad = Ip(th, ts);

            return "if (" + exprtrad.nombre + ")\n" + itrad + iptrad;
        } 
        else if(t.tipo == Token.PRINT){
        	Simbolo exprtrad;

            add(21);
            emparejar(Token.PRINT);
            exprtrad = Expr(th, ts);

            if(exprtrad.tipo == Simbolo.ENTERO){
                return "  printf(\"%d\"," + exprtrad.nombre + ");";
            }

            else {
                return "  printf(\"%f\"," + exprtrad.nombre + ");";
            }
        } 
        else{errorSintaxis(Token.ID,Token.LBRA,Token.INT,Token.FLOAT,Token.IF,Token.PRINT);}
    	return "";
    }

    // Reglas 19, 20
    public final String Ip(String th, TablaSimbolos ts){
        if(t.tipo == Token.ELSE){
            TablaSimbolos ts2;
            ts2 = new TablaSimbolos(ts);
        	String itrad;

            add(19);
            emparejar(Token.ELSE);
            itrad = I(th, ts2);
            emparejar(Token.FI);

            return "\n\nelse\n" + itrad;
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
    public final Simbolo Expr(String th, TablaSimbolos ts){
    	Simbolo etrad, exprtrad;

        if(t.tipo == Token.ID || t.tipo == Token.NUMENTERO || t.tipo == Token.NUMREAL || t.tipo == Token.PARI){
            add(22);
            etrad = E(th, ts);
            exprtrad = Exprp(th, etrad.tipo, ts);

            if(exprtrad == null){
                return new Simbolo(etrad.nombre , etrad.tipo, "");
            }
            else if (etrad.tipo == exprtrad.tipo){
                return new Simbolo(etrad.nombre + exprtrad.nombre, Simbolo.ENTERO,"");
            }
            else if (etrad.tipo == Simbolo.REAL && exprtrad.tipo == Simbolo.ENTERO){
                return new Simbolo(etrad.nombre + exprtrad.nombre, Simbolo.ENTERO,"");
            }
            else{
                return new Simbolo("itor(" + etrad.nombre + ") " + exprtrad.nombre, Simbolo.ENTERO,"");
            }

        } 
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI); }
    	return null;
    }

    //Reglas 23, 24 
    public final Simbolo Exprp(String th, int tipo, TablaSimbolos ts){
        if(t.tipo == Token.OPREL){
        	String oprellexema;
            Simbolo etrad;

            add(23);
            oprellexema = t.lexema;
            emparejar(Token.OPREL);
            etrad = E(th, ts);

            if(etrad.tipo == tipo){
                if(etrad.tipo == Simbolo.ENTERO){
                    return new Simbolo(oprellexema + "i " + etrad.nombre, etrad.tipo, "");
                }
                else{
                    return new Simbolo(oprellexema + "r " + etrad.nombre, etrad.tipo, "");
                }
            }
            else {
                return new Simbolo(oprellexema + "r " + etrad.nombre,etrad.tipo, "");
            }
       } 
        else if(t.tipo == Token.PYC || t.tipo == Token.RBRA || t.tipo == Token.ELSE || 
                t.tipo == Token.FI || t.tipo == Token.DOSP || t.tipo == Token.PARD){
            //Conjunto vacio
            add(24);
        	return null;
        }
        else{errorSintaxis(Token.RBRA,Token.PYC,Token.DOSP,Token.ELSE,Token.FI,Token.OPREL,Token.PARD);}
    	return null;
    }

    // Regla 25
    public final Simbolo E(String th,TablaSimbolos ts){
    	Simbolo ttrad, eptrad;

        if(t.tipo == Token.ID || t.tipo == Token.NUMENTERO || t.tipo == Token.NUMREAL || t.tipo == Token.PARI){
            add(25);
            ttrad = T(th, ts);
            eptrad = Ep(th, ttrad.tipo, ts);

            if(eptrad == null){
                return new Simbolo(ttrad.nombre, ttrad.tipo, "");
            }
            else if (ttrad.tipo == eptrad.tipo){
                if(eptrad.nomtrad != ""){
                     return new Simbolo("itor(" + ttrad.nombre + " " + eptrad.nombre + ") " + eptrad.nomtrad, Simbolo.REAL,"");
                }
                return new Simbolo(ttrad.nombre + eptrad.nombre, ttrad.tipo,"");
            }
            else if (ttrad.tipo == Simbolo.REAL && eptrad.tipo == Simbolo.ENTERO){
                return new Simbolo(ttrad.nombre + eptrad.nombre, ttrad.tipo ,"");
            }
            else{
                return new Simbolo("itor(" + ttrad.nombre + ") " + eptrad.nombre , Simbolo.REAL,"");
            }
        }
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI);}
    	return null;
    }

    // Reglas 26, 27
    public final Simbolo Ep(String th, int tipo, TablaSimbolos ts){
    	String opaslexema;
        Simbolo ttrad, eptrad;

        if(t.tipo == Token.OPAS){
            add(26);
            opaslexema = t.lexema;
            emparejar(Token.OPAS);
            ttrad = T(th, ts);
            eptrad = Ep(th, ttrad.tipo, ts);

            if(eptrad == null){
                if(ttrad.tipo == Simbolo.ENTERO){
                    return new Simbolo(opaslexema + "i " + ttrad.nombre, Simbolo.ENTERO, "");
                }
                else{
                    return new Simbolo(opaslexema + "r " + ttrad.nombre, Simbolo.REAL, "");
                }
            }

            else if(ttrad.tipo == tipo){
                if(ttrad.tipo == Simbolo.ENTERO){
                    return new Simbolo(opaslexema + "i " + ttrad.nombre, Simbolo.ENTERO, eptrad.nombre);
                }
                else{
                    return new Simbolo(opaslexema + "r " + ttrad.nombre  + eptrad.nombre, Simbolo.REAL, "");
                }
            }
            else {
                if(ttrad.tipo == Simbolo.ENTERO){
                    return new Simbolo(opaslexema + "r itor(" + ttrad.nombre + ")" + eptrad.nombre, Simbolo.REAL, "");
                }
                else{
                    return new Simbolo(opaslexema + "r " + ttrad.nombre  + eptrad.nombre, Simbolo.REAL, "");
                }
            }
        } 
        else if(t.tipo == Token.OPREL || t.tipo == Token.PYC || t.tipo == Token.RBRA || t.tipo == Token.ELSE || 
                t.tipo == Token.FI || t.tipo == Token.DOSP || t.tipo == Token.PARD){
            //Conjunto vacio
            add(27);

        	return null;
        }
        else{errorSintaxis(Token.RBRA,Token.PYC,Token.DOSP,Token.ELSE,Token.FI,Token.OPREL,Token.OPAS,Token.PARD);}
    	return null;
    }

    // Regla 28
    public final Simbolo T(String th, TablaSimbolos ts){
    	Simbolo ftrad, tptrad;

        if(t.tipo == Token.ID || t.tipo == Token.NUMENTERO || t.tipo == Token.NUMREAL || t.tipo == Token.PARI){
            add(28);
            ftrad = F(th, ts);
            tptrad = Tp(th, ftrad.tipo, ts);

            if(tptrad == null){
                return new Simbolo(ftrad.nombre, ftrad.tipo, "");
            }
            else if (ftrad.tipo == tptrad.tipo){
                return new Simbolo(ftrad.nombre + tptrad.nombre, ftrad.tipo,"");
            }
            else if (ftrad.tipo == Simbolo.REAL && tptrad.tipo == Simbolo.ENTERO){
                return new Simbolo(ftrad.nombre + tptrad.nombre, ftrad.tipo,"");
            }
            else{
                return new Simbolo("itor(" + ftrad.nombre + ") " + tptrad.nombre, Simbolo.REAL ,"");
            }
        }
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI);}
    	return null;
    }

    // Reglas 29, 30
    public final Simbolo Tp(String th, int tipo, TablaSimbolos ts){
    	String opmultlexema;
        Simbolo ftrad, tptrad;

        if(t.tipo == Token.OPMUL){
            add(29);
            opmultlexema = t.lexema;
            emparejar(Token.OPMUL);
            ftrad = F(th, ts);
            tptrad = Tp(th, ftrad.tipo, ts);

            if(tptrad == null){
                if(tipo == Simbolo.ENTERO && ftrad.tipo == Simbolo.ENTERO){
                    return new Simbolo(opmultlexema + "i " + ftrad.nombre, Simbolo.ENTERO, "");
                }
                else{
                    if(ftrad.tipo == Simbolo.ENTERO){
                        return new Simbolo(opmultlexema + "r itor(" + ftrad.nombre + ")", Simbolo.REAL, "");
                    }
                    else{
                        return new Simbolo(opmultlexema + "r " + ftrad.nombre, Simbolo.REAL, "");
                    }
                }
            }

            else if(ftrad.tipo == tipo){
                if(ftrad.tipo == Simbolo.ENTERO){
                    return new Simbolo(opmultlexema + "i " + ftrad.nombre +  tptrad.nombre, Simbolo.ENTERO, "");
                }
                else{
                    return new Simbolo(opmultlexema + "r " + ftrad.nombre +  tptrad.nombre, Simbolo.REAL, "");
                }
            }
            else {
                if(ftrad.tipo == Simbolo.ENTERO){
                    return new Simbolo(opmultlexema + "r itor(" + ftrad.nombre + ")" + tptrad.nombre, Simbolo.REAL, "");
                }
                else{
                    return new Simbolo(opmultlexema + "r " + ftrad.nombre + tptrad.nombre, Simbolo.REAL, "");
                }
            }
        }
        else if(t.tipo == Token.OPREL || t.tipo == Token.PYC || t.tipo == Token.RBRA || t.tipo == Token.ELSE || 
                t.tipo == Token.OPAS || t.tipo == Token.FI || t.tipo == Token.DOSP || t.tipo == Token.PARD){
            //Conjunto Vacio
            add(30);

        	return null;
        }
        else{errorSintaxis(Token.RBRA,Token.PYC,Token.DOSP,Token.ELSE,Token.FI,Token.OPREL,Token.OPAS,Token.OPMUL,Token.PARD);}
    	return null;
    }

    // Reglas 31, 32, 33, 34
    public final Simbolo F(String th, TablaSimbolos ts){
        if(t.tipo == Token.ID){
        	String idlexema;
            idlexema = t.lexema;
        	Simbolo s = ts.buscar(idlexema);
            if(s == null){errorSemantico(ERRNODECL,t);}
            idlexema = s.nomtrad;
            add(31);
            emparejar(Token.ID);

            return new Simbolo(s.nomtrad, s.tipo,"");
        }
        else if(t.tipo == Token.NUMENTERO){
        	String numenterolexema;

            add(32);
            numenterolexema = t.lexema;
            emparejar(Token.NUMENTERO);

            return new Simbolo(numenterolexema, Simbolo.ENTERO,"");
        }
        else if(t.tipo == Token.NUMREAL){
        	String numreallexema;

            add(33);
            numreallexema = t.lexema;
            emparejar(Token.NUMREAL);

            return new Simbolo(numreallexema, Simbolo.REAL,"");
        }
        else if(t.tipo == Token.PARI){
        	Simbolo exprtrad;

            add(34);
            emparejar(Token.PARI);
            exprtrad = Expr(th, ts);
            emparejar(Token.PARD);

            return new Simbolo("(" + exprtrad.nombre + ")", exprtrad.tipo, "");
        }
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI);}
    	return null;
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