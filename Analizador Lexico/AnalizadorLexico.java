import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class AnalizadorLexico {

    private final RandomAccessFile entrada;
    int fila = 1, colum = 1, pos = 0;
    Token t = new Token();

    AnalizadorLexico(final RandomAccessFile entrada) {
        this.entrada = entrada;
    }

    //Nos devuelve que tipo de palabra reservada es
    public int palabras_reservadas(final String p) {
        switch (p) {
            case "class":
                return Token.CLASS;
            case "fun":
                return Token.FUN;
            case "int":
                return Token.INT;
            case "float":
                return Token.FLOAT;
            case "if":
                return Token.IF;
            case "else":
                return Token.ELSE;
            case "fi":
                return Token.FI;
            case "print":
                return Token.PRINT;
            default:
                return Token.ID;
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
                    case '*':
                    case '/': return 15;
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
            case 15: return -1;
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
            default:
                return -1; 
        }
    } 

    //Lee un caracter
    public char read(){
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

    //Comprueba si es un estado en el que se encuentra en medio
    public boolean e_final(int e) {
        final int[] inter = new int[] { 0, 7, 9, 11, 12, 16, 18, 20, 24 };
        Arrays.sort(inter);
        int res = Arrays.binarySearch(inter, e); 
        return res > 0 ? false : true; 
    }

    public Token siguienteToken(){
        int estado = 0;
        final char c = read();
        do{
            estado = delta(estado, c);
            if(e_final(estado)){

            }

        }while(true);
    }
}
