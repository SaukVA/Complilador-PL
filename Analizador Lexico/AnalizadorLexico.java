import java.io.RandomAccessFile;

public class AnalizadorLexico {

    private final RandomAccessFile entrada;

    AnalizadorLexico(final RandomAccessFile entrada) {
        this.entrada = entrada;
    }

    private int delta( int estado, int c){
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
                        else {return (Integer)null;} // como se lanzaría el error????
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
                                || (c>='0' && c<='9')){ return 24;} // Se debería de poner así???????
                    else {return 25;}
            case 25: return -1;
            default:
                return (Integer)null; // como se lanzaría el error????
        }
    } 

    public Token siguienteToken(){
        return null;
    }
}
