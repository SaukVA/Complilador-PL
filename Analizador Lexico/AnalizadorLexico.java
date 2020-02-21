import java.io.RandomAccessFile;

public class AnalizadorLexico {

    private final RandomAccessFile entrada;

    AnalizadorLexico(final RandomAccessFile entrada) {
        this.entrada = entrada;
    }

    public Token siguienteToken(){
        return null;
    }
}
