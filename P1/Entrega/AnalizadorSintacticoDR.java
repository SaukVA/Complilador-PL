
public class AnalizadorSintacticoDR{

    private AnalizadorLexico al;
    private Token t;
    private boolean f = true;
    private StringBuilder numR;

    //Contructor de la clase AnalizadorSintacticoDR
    public AnalizadorSintacticoDR(AnalizadorLexico al){
        this.al = al;
        this.numR = new StringBuilder();
        this.t = new Token();
        t = al.siguienteToken();
    }

    //---------------------------------->REGLAS<----------------------------------
    // En cada una de esta regla vamos anotando los numeros de regla
    // y ejecutando los siguientes pasos de la regla
    
    // Regla 1
    public final void S(){
        if(t.tipo == Token.CLASS){
            add(1); // añadimos la regla '1'
            emparejar(Token.CLASS);
            emparejar(Token.ID);
            emparejar(Token.LBRA);
            M();
            emparejar(Token.RBRA);
        }else {errorSintaxis(Token.CLASS);}
    }

    // Reglas 2,3,4
    public final void M(){
        if(t.tipo == Token.FUN){
            add(2);
            Fun();
            M();
        }
        else if(t.tipo == Token.CLASS){
            add(3);
            S();
            M();
        }
        else if(t.tipo == Token.RBRA || t.tipo == Token.PRINT || t.tipo == Token.IF || t.tipo == Token.ID || 
                t.tipo == Token.LBRA || t.tipo == Token.INT || t.tipo == Token.FLOAT){
             //Conjunto vacio
             add(4);
        } 
        else{errorSintaxis(Token.CLASS,Token.ID,Token.LBRA,Token.RBRA,Token.FUN,Token.INT,Token.FLOAT,Token.IF,Token.PRINT);}
    }

    // Regla 5
    public final void Fun(){
        if(t.tipo == Token.FUN){
            add(5);
            emparejar(Token.FUN);
            emparejar(Token.ID);
            A();
            emparejar(Token.LBRA);
            M();
            Cod();
            emparejar(Token.RBRA);

        }else{errorSintaxis(Token.FUN);}
    }

    // Regla 6
    public final void A(){
        if(t.tipo == Token.INT || t.tipo == Token.FLOAT){
            add(6);
            DV();
            Ap();
        }
        else{errorSintaxis(Token.INT,Token.FLOAT);}
    }

    // Regla 7, 8
    public final void Ap(){
        if(t.tipo == Token.PYC){
            add(7);
            emparejar(Token.PYC);
            DV();
            Ap();
        } else if(t.tipo == Token.LBRA){
            //Conjunto vacio
            add(8);
        }
        else{errorSintaxis(Token.LBRA,Token.PYC);}
    }

    // Regla 9
    public final void DV(){
        if(t.tipo == Token.INT || t.tipo == Token.FLOAT){
            add(9);
            Tipo();
            emparejar(Token.ID);
        }
        else{errorSintaxis(Token.INT, Token.FLOAT);}
    }
    
    // Reglas 10, 11
    public final void Tipo(){
        if(t.tipo == Token.INT){
            add(10);
            emparejar(Token.INT);
        }
        else if(t.tipo == Token.FLOAT){
            add(11);
            emparejar(Token.FLOAT);
        }
        else{errorSintaxis(Token.INT,Token.FLOAT);}
    } 

    // Regla 12
    public final void Cod(){
        if( t.tipo == Token.PRINT || t.tipo == Token.IF || t.tipo == Token.LBRA || 
            t.tipo == Token.INT || t.tipo == Token.FLOAT|| t.tipo == Token.ID){
                add(12);
                I();
                Codp();
        }
        else{errorSintaxis(Token.ID,Token.LBRA,Token.INT,Token.FLOAT,Token.IF,Token.PRINT);}
    }

    // Reglas 13, 14
    public final void Codp(){
        if(t.tipo == Token.PYC){
            add(13);
            emparejar(Token.PYC);
            I();
            Codp();
        }else if(t.tipo == Token.RBRA){
            //Conjunto vacio
            add(14);
        } 
        else{errorSintaxis(Token.RBRA,Token.PYC);}
    }

    // Reglas 15, 16, 17, 18, 21
    public final void I(){
        if(t.tipo == Token.INT || t.tipo == Token.FLOAT){
            add(15);
            DV();
        } 
        else if(t.tipo == Token.LBRA){
            add(16);
            emparejar(Token.LBRA);
            Cod();
            emparejar(Token.RBRA);
        } 
        else if(t.tipo == Token.ID){
            add(17);
            emparejar(Token.ID);
            emparejar(Token.ASIG);
            Expr();
        }
        else if(t.tipo == Token.IF){
            add(18);
            emparejar(Token.IF);
            Expr();
            emparejar(Token.DOSP);
            I();
            Ip();
        } 
        else if(t.tipo == Token.PRINT){
            add(21);
            emparejar(Token.PRINT);
            Expr();
        } 
        else{errorSintaxis(Token.ID,Token.LBRA,Token.INT,Token.FLOAT,Token.IF,Token.PRINT);}
    }

    // Reglas 19, 20
    public final void Ip(){
        if(t.tipo == Token.ELSE){
            add(19);
            emparejar(Token.ELSE);
            I();
            emparejar(Token.FI);
        }
        else if(t.tipo == Token.FI){
            add(20);
            emparejar(Token.FI);
        }
        else{errorSintaxis(Token.ELSE,Token.FI);}
    }

    //Regla 22
    public final void Expr(){
        if(t.tipo == Token.ID || t.tipo == Token.NUMENTERO || t.tipo == Token.NUMREAL || t.tipo == Token.PARI){
            add(22);
            E();
            Exprp();
        } 
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI); }
    }

    //Reglas 23, 24 
    public final void Exprp(){
        if(t.tipo == Token.OPREL){
            add(23);
            emparejar(Token.OPREL);
            E();
        } 
        else if(t.tipo == Token.PYC || t.tipo == Token.RBRA || t.tipo == Token.ELSE || 
                t.tipo == Token.FI || t.tipo == Token.DOSP || t.tipo == Token.PARD){
            //Conjunto vacio
            add(24);
        }
        else{errorSintaxis(Token.RBRA,Token.PYC,Token.DOSP,Token.ELSE,Token.FI,Token.OPREL,Token.PARD);}
    }

    // Regla 25
    public final void E(){
        if(t.tipo == Token.ID || t.tipo == Token.NUMENTERO || t.tipo == Token.NUMREAL || t.tipo == Token.PARI){
            add(25);
            T();
            Ep();
        }
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI);}
    }

    // Reglas 26, 27
    public final void Ep(){
        if(t.tipo == Token.OPAS){
            add(26);
            emparejar(Token.OPAS);
            T();
            Ep();
        } 
        else if(t.tipo == Token.OPREL || t.tipo == Token.PYC || t.tipo == Token.RBRA || t.tipo == Token.ELSE || 
                t.tipo == Token.FI || t.tipo == Token.DOSP || t.tipo == Token.PARD){
            //Conjunto vacio
            add(27);
        }
        else{errorSintaxis(Token.RBRA,Token.PYC,Token.DOSP,Token.ELSE,Token.FI,Token.OPREL,Token.OPAS,Token.PARD);}
    }

    // Regla 28
    public final void T(){
        if(t.tipo == Token.ID || t.tipo == Token.NUMENTERO || t.tipo == Token.NUMREAL || t.tipo == Token.PARI){
            add(28);
            F();
            Tp();
        }
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI);}
    }

    // Reglas 29, 30
    public final void Tp(){
        if(t.tipo == Token.OPMUL){
            add(29);
            emparejar(Token.OPMUL);
            F();
            Tp();
        }
        else if(t.tipo == Token.OPREL || t.tipo == Token.PYC || t.tipo == Token.RBRA || t.tipo == Token.ELSE || 
                t.tipo == Token.OPAS || t.tipo == Token.FI || t.tipo == Token.DOSP || t.tipo == Token.PARD){
            //Conjunto Vacio
            add(30);
        }
        else{errorSintaxis(Token.RBRA,Token.PYC,Token.DOSP,Token.ELSE,Token.FI,Token.OPREL,Token.OPAS,Token.OPMUL,Token.PARD);}
    }

    // Reglas 31, 32, 33, 34
    public final void F(){
        if(t.tipo == Token.ID){
            add(31);
            emparejar(Token.ID);
        }
        else if(t.tipo == Token.NUMENTERO){
            add(32);
            emparejar(Token.NUMENTERO);
        }
        else if(t.tipo == Token.NUMREAL){
            add(33);
            emparejar(Token.NUMREAL);
        }
        else if(t.tipo == Token.PARI){
            add(34);
            emparejar(Token.PARI);
            Expr();
            emparejar(Token.PARD);
        }
        else{errorSintaxis(Token.ID,Token.NUMENTERO,Token.NUMREAL,Token.PARI);}
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

}