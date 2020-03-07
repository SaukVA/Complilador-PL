import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalizadorLexico {

    private final RandomAccessFile entrada;
    int fila = 1, colum = 1, anterior = 0;
    Token t = new Token();
    boolean comen = false;

    // Constructor de la clase AnalizadorLexico
    AnalizadorLexico(final RandomAccessFile entrada) {
        this.entrada = entrada;
    }

    //Nos devuelve que tipo de palabra reservada es
    public int palabras_reservadas(final String p) {
        switch (p) {
            case "class": return Token.CLASS;
            case "fun": return Token.FUN;
            case "int": return Token.INT;
            case "float": return Token.FLOAT;
            case "if": return Token.IF;
            case "else": return Token.ELSE;
            case "fi": return Token.FI;
            case "print": return Token.PRINT;
            default: return Token.ID;
        }
    }

    //Nos devuelve el estado en el que se queda al comprobar el
    public int delta(final int estado, final int c) {
        switch(estado){
            case 0: 
                switch(c){
                    case '(': return 1; 
                    case ')': return 2;
                    case ':': return 3;
                    case '{': return 4;
                    case '}': return 5;
                    case ';': return 6;
                    case '<': return 7;
                    case '>': return 9;
                    case '!': return 11;
                    case '=': return 12;
                    case '+': 
                    case '-': return 14;
                    case '*': return 15;
                    case '/': return 26;
                    default: if(c>='0' && c<='9'){return 16;}
                        else if((c>='A' && c<='Z') || (c>='a' && c<='z')){ return 24;}
                        else return -1; 
                }
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6: return -1;
            case 7:if(c=='='){return 8;}
                else{return 10;}
            case 8:return -1;
            case 9: if(c=='='){return 8;}
                else{return 10;}
            case 10: return -1;
            case 11: if(c=='='){return 8;}
                else{return -1;}
            case 12: if(c=='='){return 8;}
                else{return 13;}
            case 13:
            case 14:
            case 15: if(c=='/' && comen){return 28;}else{return 27;}
            case 16: if(c>='0' && c<='9'){return 16;}
                else if(c=='.'){return 18;}
                else return 17;
            case 17: return -1;
            case 18: if(c>='0' && c<='9'){return 20;}
                else return 19;
            case 19: return -1;
            case 20: if(c>='0' && c<='9'){return 20;}
                else return 21;
            case 21: return -1;
            case 24: if((c>='A' && c<='Z') || (c>='a' && c<='z') 
                                || (c>='0' && c<='9')){ return 24;} 
                    else {return 25;}
            case 25: return -1;
            case 26: if(c=='*'){return 29;}else{return 27;}
            case 27:
            case 28:
            case 29: return -1;
            default:
                return -1; 
        }
    } 

    //Lee un caracter
    public char leer(){
       char c;
       try{
           c = (char)entrada.readByte();
           return c;           
       }catch(final EOFException e){
           return (char)-1;
       }
       catch(final IOException e){
            return ' ';
       }
    }

    //Retrocede a al estado anterior
    public void anterior(){
        try { entrada.seek(anterior);} 
        catch (IOException ex) {
            Logger.getLogger(AnalizadorLexico.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Comprueba si es un estado en el que se encuentra en medio o es un estado final
    public boolean no_final(int e) {
        final int[] inter = new int[] { 0, 7, 9, 11, 12, 15, 16, 18, 20, 24, 26};
        Arrays.sort(inter);
        int res = Arrays.binarySearch(inter, e); 
        return res > 0 ? true : false; 
    }

    //Devuelve el Token siguiente
    public Token siguienteToken(){
        int estado = 0;
        t.lexema = "";
        char c = leer();
        do{

            estado = delta(estado, c);
            
            //Cuando no esun estado final y es distinto de -1
            if(no_final(estado)){
                if(estado == 0 ){ t.lexema="";} else{ t.lexema+=c;}
                colum++;
                anterior++;
                c = leer();
            }
            //Cuando su estado es -1 puede darse por '\n', '\t', ' ' 
            // o final de fichero, si se da por cualquier otra razon 
            // se lanza error.
            else if(estado == -1){
                switch(c){
                    case ' ':
                    case '\t':
                        colum++;
                        estado = 0;
                        anterior++;
                        break;
                    case '\n':
                        colum = 1;
                        fila ++;
                        estado = 0;
                        anterior ++;
                        break;
                    case (char) -1:
                        if(!comen){
                            t.tipo = Token.EOF;
                            t.fila = fila;
                            t.columna = colum;
                            return t;
                        }else{System.err.println("Error lexico: fin de fichero inesperado");System.exit(-1);}
                    default:
                        if(!comen){System.err.println("Error lexico ("+fila+","+colum+"): caracter '"+ c +"' incorrecto");System.exit(-1);}
                        else{colum++; estado = 0; anterior++;}
                }
                c = leer();
            }
            // Cuando llegamos a un estado final se deven realizar las 
            // operaciones correspondientes al estado declarado
            else{
                if(estado == 19){
                    t.tipo = Token.NUMENTERO;
                    t.lexema = t.lexema.substring(0, t.lexema.length()-1);
                    anterior();
                    colum--;
                    anterior --;
                    anterior();
                    estado = 0;
                }
                else{
                    switch(estado){
                        case 10:t.tipo = Token.OPREL; anterior(); estado = 0; break;
                        case 13:t.tipo = Token.ASIG; anterior(); estado = 0; break;
                        case 17:t.tipo = Token.NUMENTERO; anterior(); estado = 0; break;
                        case 21:t.tipo = Token.NUMREAL; anterior(); estado = 0; break;
                        case 25:t.tipo = palabras_reservadas(t.lexema); anterior(); estado = 0; break;
                        case 27:t.tipo = Token.OPMUL; anterior(); estado = 0; break;
                        default:
                            t.lexema += c;
                            colum++;
                            anterior ++;
                            switch(estado){
                                case 1: t.tipo = Token.PARI; break;
                                case 2: t.tipo = Token.PARD; break;
                                case 3: t.tipo = Token.DOSP; break;
                                case 4: t.tipo = Token.LBRA; break;
                                case 5: t.tipo = Token.RBRA; break;
                                case 6: t.tipo = Token.PYC; break;
                                case 8: t.tipo = Token.OPREL; break;
                                case 14: t.tipo = Token.OPAS; break;
                                case 29: comen = true; estado = 0; break;
                            }
                        break;
                    }
                }
                if(!comen){
                    t.fila = fila;
                    t.columna = colum -t.lexema.length();
                    return t;
                }
                if (estado == 28){comen = false; estado = 0; t.lexema = "";}
                c = leer();
            }
        }while(true);
    }
}
