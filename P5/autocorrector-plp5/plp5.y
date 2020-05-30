/*------------------------------ plp5.y -------------------------------*/
%token prg var integer real caracter prn read_fun
%token if_con else_con
%token while_con toChr toInt
%token id numentero numreal
%token coma pyc dosp
%token lbra rbra pari pard cori cord
%token opas oprel opmd
%token asig ptopto

%{

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <iostream>


using namespace std;

#include "comun.h"
#include "TablaSimbolos.h"

// variables y funciones del A. Léxico
extern int ncol,nlin,findefichero;


extern int yylex();
extern char *yytext;
extern FILE *yyin;


int yyerror(char *s);
void errorSemantico(int nerror,char *lexema,int fila,int columna);
string convert(int n);
int NuevaTemp();
int NuevaVariable(char *s, int col, int lin);
string NuevaL();
string funTrad(char *s);

const int ERRYADECL=1,ERRNODECL=2,ERRTIPOS=3,ERRNOSIMPLE=4,ERRNOENTERO=5;

string lexema, s1, s2;
int cvar = 0;
int ctemp = 16000;
int cL = 0;
TablaSimbolos *tsa = new TablaSimbolos(NULL); 

%}

%%


S           :  prg id dosp BlVar Bloque { 
                                          cout << $5.cod + "halt\n"; 
                                          int tk = yylex();
                                          if (tk != 0) yyerror("");
                                        }
            ;

Bloque      : lbra SeqInstr rbra { $$.cod = $2.cod;}  
            ;

BlVar       : var Decl pyc { ;/*nada*/ }
            ;

Decl        : Decl pyc DVar { ;/*nada*/ }
            | DVar { ;/*nada*/ }
            ;

DVar        : Tipo dosp { $$.th = $1.cod; } LIdent { ;/*nada*/ }
            ;

TipoSimple  : integer { $$.cod = "ENTERO"; }
            | real { $$.cod = "REAL"; }
            ;

Tipo        : TipoSimple { $$.cod = $1.cod; }
            ;

LIdent      : LIdent coma 
              id  {
                    if(tsa->buscar($3.lexema) == NULL){
                      unsigned tmp = NuevaVariable($3.lexema,ncol,nlin);
                      unsigned tipo = $0.th == "ENTERO" ? ENTERO : REAL;
                      struct Simbolo s = {$3.lexema, tipo, tmp, unsigned(1)};
                      tsa->anyadir(s);
                    }
                    else{ errorSemantico(ERR_YADECL,$3.nlin,$3.ncol,$3.lexema); }
                  }
            | id  {
                    if(tsa->buscar($1.lexema) == NULL){
                        unsigned tmp = NuevaVariable($1.lexema,ncol,nlin);
                        unsigned tipo = $0.th == "ENTERO" ? ENTERO : REAL;
                        struct Simbolo s = {$1.lexema, tipo, tmp, unsigned(1)};
                        tsa->anyadir(s);
                    }
                    else{ errorSemantico(ERR_YADECL,$1.nlin,$1.ncol,$1.lexema); }
                  }   

SeqInstr    : SeqInstr pyc { $$.nlin = ctemp; } Instr { $$.cod = $1.cod + $4.cod; ctemp = $3.nlin; }
            | { $$.nlin = ctemp; } Instr { $$.cod = $2.cod; ctemp = $1.nlin;}
            ;

Instr       : Ref asig 
              Expr    {
                        if($1.tipo == $3.tipo){
                          $$.cod = $3.cod + "mov " + convert($3.dir) + " " + convert($1.dir) + "\n";
                        }
                        else if($1.tipo == REAL && $3.tipo == ENTERO){
                          int tmp = NuevaTemp();
                          $$.cod = $3.cod+ 
                                    "mov "  + convert($3.dir) + " A \n" + 
                                    "itor " + "\n"+
                                    "mov A " + convert(tmp) + "\n" +
                                    "mov "  + convert(tmp) + " " + convert($1.dir) + "\n";
                        }
                        else{
                          errorSemantico(ERR_ASIG,$2.nlin,$2.ncol,$2.lexema);
                        }
                      }
            | Bloque  { $$.cod = $1.cod; }
            | prn     { lexema = $1.lexema; $$.cod = lexema == "print" ? "" : "wrl\n"; } 
              Expr    {
                        if($3.tipo == ENTERO){
                          $$.cod = $3.cod + "wri " + convert($3.dir) + "\n" + $2.cod;
                        }
                        else{
                          $$.cod = $3.cod + "wrr " + convert($3.dir) + "\n" + $2.cod;
                        }
                      }
            | read_fun 
              Ref     {
                        if($2.tipo == ENTERO){
                            $$.cod = $2.cod + "rdi " + convert($2.dir) + "\n"; 
                        }
                        else{
                            $$.cod = $2.cod + "rdr " + convert($2.dir) + "\n"; 
                        }
                      } 
            | if_con Expr dosp 
              Instr   {
                        if($2.tipo != ENTERO){ errorSemantico(ERR_IFWHILE,$1.nlin,$1.ncol,$1.lexema);}
                        string tmp = NuevaL();
                        $$.cod = $2.cod + 
                                  "mov " + convert($2.dir) + " A \n" +
                                  "jz L" + tmp + " \n" +
                                  $4.cod + 
                                  "L" + tmp + " \n";
                      }
            | if_con Expr dosp Instr else_con 
              Instr   {
                        if($2.tipo != ENTERO){ errorSemantico(ERR_IFWHILE,$1.nlin,$1.ncol,$1.lexema);}
                        string tmp = NuevaL();
                        string tmp2 = NuevaL();
                        $$.cod = $2.cod + 
                                  "mov " + convert($2.dir) + " A \n" +
                                  "jz L" + tmp + " \n"  +
                                  $4.cod  +
                                  "jmp L" + tmp2 + " \n" +
                                  "L" + tmp + " \n"     +
                                  $6.cod  +
                                  "L" + tmp2 + " \n";
                      }
            | while_con Expr dosp 
              Instr   {
                        if($2.tipo != ENTERO){ errorSemantico(ERR_IFWHILE,$1.nlin,$1.ncol,$1.lexema);}
                        string tmp = NuevaL();
                        string tmp2 = NuevaL();
                        $$.cod =  "L" + tmp + " \n" +
                                  $2.cod + 
                                  "mov " + convert($2.dir) + " A \n" +
                                  "jz L" + tmp2 + " \n" +
                                  $4.cod +
                                  "jmp L" + tmp + " \n" + 
                                  "L" + tmp2 + " \n";
                      }
            ;

Expr        : Esimple   { $$.cod = $1.cod; $$.tipo = $1.tipo; $$.dir = $1.dir;} 
            | Esimple oprel
              Esimple   {
                          int tmp = NuevaTemp();
                          $$.dir = tmp;
                          if($1.tipo == ENTERO && $3.tipo == ENTERO){
                            $$.cod =  $1.cod + $3.cod +
                                      "mov " + convert($1.dir) + " A \n" +
                                      funTrad($2.lexema) + "i " + convert($3.dir) + "\n" +
                                      "mov A " + convert(tmp) + "\n";
                            $$.tipo = ENTERO; 
                          }
                          else if($1.tipo == REAL && $3.tipo == ENTERO){
                            int tmp2 = NuevaTemp();
                            $$.cod =  $1.cod + $3.cod +
                                      "mov " + convert($3.dir) + " A \n" +
                                      "itor \n" +
                                      "mov A " + convert(tmp2) + "\n" +
                                      "mov " + convert($1.dir) + " A \n" +
                                      funTrad($2.lexema) + "r " + convert(tmp2) + "\n" +
                                      "mov A " + convert(tmp) + "\n";
                            $$.tipo = ENTERO;
                          }
                          else if($1.tipo == ENTERO && $3.tipo == REAL){
                            $$.cod =  $1.cod + $3.cod +
                                      "mov " + convert($1.dir) + " A \n" +
                                      "itor \n" +
                                      funTrad($2.lexema) + "r " + convert($3.dir) + "\n" +
                                      "mov A " + convert(tmp) + "\n";
                            $$.tipo = ENTERO;
                          }
                          else if($1.tipo == REAL && $3.tipo == REAL){
                            $$.cod =  $1.cod + $3.cod +
                                      "mov " + convert($1.dir) + " A \n" +
                                      funTrad($2.lexema) + "r " + convert($3.dir) + "\n" +
                                      "mov A " + convert(tmp) + "\n";
                            $$.tipo = ENTERO;
                          }

                          // Falta por implementar la opcion CHAR y CHAR

                          else{
                            //No deberia llegar si no se ha implementado los char
                            if($1.tipo != ENTERO && $1.tipo != REAL){
                              msgErrorOperador(NUMERICO,$1.lexema,$1.nlin,$1.ncol,ERR_OPIZQ);
                            }
                            else{
                              msgErrorOperador(NUMERICO,$3.lexema,$3.nlin,$3.ncol,ERR_OPDER);
                            }
                          }
                        }
            ;

Esimple     : Term { $$.cod = $1.cod; $$.tipo = $1.tipo; $$.dir = $1.dir; }
            | opas 
              Term  {
                      int tmp = NuevaTemp();
                      $$.dir = tmp; 
                      $$.tipo = $2.tipo;
                      if($2.tipo == ENTERO){
                        $$.cod =  $2.cod +
                                  "mov #0 A \n" + 
                                  funTrad($1.lexema) + "i " + convert($2.dir) + "\n" +
                                  "mov A " + convert(tmp) + "\n";
                      }
                      else{
                        $$.cod =  $2.cod +
                                  "mov $0 A \n" +
                                  funTrad($1.lexema) + "r " + convert($2.dir) + "\n" +
                                  "mov A " + convert(tmp) + "\n";
                      }
                    }
            | Esimple opas 
              Term {
                      int tmp = NuevaTemp();
                      $$.dir = tmp;
                      if($1.tipo == ENTERO && $3.tipo == ENTERO){
                        $$.cod =  $1.cod + $3.cod +
                                  "mov " + convert($1.dir) + " A \n" +
                                  funTrad($2.lexema) + "i " + convert($3.dir) + "\n" +
                                  "mov A " + convert(tmp) + "\n";
                        $$.tipo = ENTERO; 
                      }
                      else if($1.tipo == REAL && $3.tipo == ENTERO){
                        int tmp2 = NuevaTemp();
                        $$.cod =  $1.cod + $3.cod +
                                  "mov " + convert($3.dir) + " A \n" +
                                  "itor \n" +
                                  "mov A " + convert(tmp2) + "\n" +
                                  "mov " + convert($1.dir) + " A \n" +
                                  funTrad($2.lexema) + "r " + convert(tmp2) + "\n" +
                                  "mov A " + convert(tmp) + "\n";
                        $$.tipo = REAL;
                      }
                      else if($1.tipo == ENTERO && $3.tipo == REAL){
                        $$.cod =  $1.cod + $3.cod +
                                  "mov " + convert($1.dir) + " A \n" +
                                  "itor \n" +
                                  funTrad($2.lexema) + "r " + convert($3.dir) + "\n" +
                                  "mov A " + convert(tmp) + "\n";
                        $$.tipo = REAL;
                      }
                      else if($1.tipo == REAL && $3.tipo == REAL){
                        $$.cod =  $1.cod + $3.cod +
                                  "mov " + convert($1.dir) + " A \n" +
                                  funTrad($2.lexema) + "r " + convert($3.dir) + "\n" +
                                  "mov A " + convert(tmp) + "\n";
                        $$.tipo = REAL;
                      }

                      // Falta por implementar la opcion CHAR y CHAR

                      else{
                        //No deberia llegar si no se ha implementado los char
                        if($1.tipo != ENTERO && $1.tipo != REAL){
                          msgErrorOperador(NUMERICO,$1.lexema,$1.nlin,$1.ncol,ERR_OPIZQ);
                        }
                        else{
                          msgErrorOperador(NUMERICO,$3.lexema,$3.nlin,$3.ncol,ERR_OPDER);
                        }
                      }
                    }
            ;

Term        : Factor    { $$.cod = $1.cod; $$.tipo = $1.tipo; $$.dir = $1.dir; }
            | Term opmd 
              Factor  {
                        int tmp = NuevaTemp();
                        $$.dir = tmp;
                        if($1.tipo == ENTERO && $3.tipo == ENTERO){
                          $$.cod =  $1.cod + $3.cod +
                                    "mov " + convert($1.dir) + " A \n" +
                                    funTrad($2.lexema) + "i " + convert($3.dir) + "\n" +
                                    "mov A " + convert(tmp) + "\n";
                          $$.tipo = ENTERO; 
                        }
                        else if($1.tipo == REAL && $3.tipo == ENTERO){
                          int tmp2 = NuevaTemp();
                          $$.cod =  $1.cod + $3.cod +
                                    "mov " + convert($3.dir) + " A \n" +
                                    "itor \n" +
                                    "mov A " + convert(tmp2) + "\n" +
                                    "mov " + convert($1.dir) + " A \n" +
                                    funTrad($2.lexema) + "r " + convert(tmp2) + "\n" +
                                    "mov A " + convert(tmp) + "\n";
                          $$.tipo = REAL;
                        }
                        else if($1.tipo == ENTERO && $3.tipo == REAL){
                          $$.cod =  $1.cod + $3.cod +
                                    "mov " + convert($1.dir) + " A \n" +
                                    "itor \n" +
                                    funTrad($2.lexema) + "r " + convert($3.dir) + "\n" +
                                    "mov A " + convert(tmp) + "\n";
                          $$.tipo = REAL;
                        }
                        else if($1.tipo == REAL && $3.tipo == REAL){
                          $$.cod =  $1.cod + $3.cod +
                                    "mov " + convert($1.dir) + " A \n" +
                                    funTrad($2.lexema) + "r " + convert($3.dir) + "\n" +
                                    "mov A " + convert(tmp) + "\n";
                          $$.tipo = REAL;
                        }

                        // Falta por implementar la opcion CHAR y CHAR

                        else{
                          //No deberia llegar si no se ha implementado los char
                          if($1.tipo != ENTERO && $1.tipo != REAL){
                            msgErrorOperador(NUMERICO,$1.lexema,$1.nlin,$1.ncol,ERR_OPIZQ);
                          }
                          else{
                            msgErrorOperador(NUMERICO,$3.lexema,$3.nlin,$3.ncol,ERR_OPDER);
                          }
                        }
                      }
            ;

Factor      : pari Expr pard { $$.cod = $2.cod; $$.tipo = $2.tipo; $$.dir = $2.dir; }
            | Ref       { 

                          int tmp = NuevaTemp();
                          $$.cod = "mov " + convert($1.dir) + " " + convert(tmp) + "\n";
                          $$.tipo = $1.tipo;
                          $$.dir = tmp;
                        }
            | numentero {
                          int tmp = NuevaTemp();
                          lexema = $1.lexema;
                          $$.cod = "mov #" + lexema + " " + convert(tmp) + "\n";
                          $$.tipo = ENTERO;
                          $$.dir = tmp;
                        }
            | numreal   {
                          int tmp = NuevaTemp();
                          lexema = $1.lexema;
                          $$.cod = "mov $" + lexema + " " + convert(tmp) + "\n";
                          $$.tipo = REAL;
                          $$.dir = tmp;
                        }
            | toInt pari Esimple 
              pard      {
                          if($3.tipo == ENTERO){
                              $$.dir = $3.dir;
                              $$.cod = $3.cod;
                              $$.tipo = $3.tipo;
                          }
                          else if ($3.tipo == REAL){
                              int tmp = NuevaTemp();
                              $$.dir = tmp;
                              $$.tipo = ENTERO;
                              $$.cod =  $3.cod +
                                        "mov " + convert($3.dir) + " A \n" +
                                        "rtoi \n" +
                                        "mov A " + convert(tmp) + "\n";
                          }
                        }
            ;

Ref         : id  {
                    struct Simbolo* s = tsa->buscar($1.lexema);
                    if(s == NULL){
                      errorSemantico(ERR_NODECL,$1.ncol,$1.nlin,$1.lexema);
                    }
                    else{
                        $$.tipo = s->tipo;
                        $$.dir = s->dir;
                    }
                  }
            ;

%%

string convert(int n){

  string s = std::to_string(n);
  return s;
}

int NuevaTemp(){
  int temp = ctemp;

  if(ctemp > 16383){ errorSemantico(ERR_MAXTEMP,1,1,NULL);}
  else{ctemp++;}

  return temp;
}

string NuevaL(){
  cL++;
  return convert(cL);
}

int NuevaVariable(char *s, int col, int lin){
  int temp = cvar;

  if(cvar > 15999){ errorSemantico(ERR_NOCABE,lin,col,s);}
  else{cvar++;}

  return temp;
}

string funTrad(char *s){
  string temp = s;
  string result = "";

  if(temp == "+"){result = "add";}
  else if(temp == "-"){result = "sub";}
  else if(temp == "*"){result = "mul";}
  else if(temp == "/"){result = "div";}
  else if(temp == "=="){result = "eql";}
  else if(temp == "!="){result = "neq";}
  else if(temp == ">"){result = "gtr";}
  else if(temp == ">="){result = "geq";}
  else if(temp == "<"){result = "lss";}
  else if(temp == "<="){result = "leq";}

  return result;
}

void errorSemantico(int nerror,int fila,int columna,const char *s)
{
    fprintf(stderr,"Error semantico (%d,%d): ",fila,columna);
    switch (nerror) {
        case ERR_YADECL: fprintf(stderr,"variable '%s' ya declarada\n",s);
               break;
        case ERR_NODECL: fprintf(stderr,"variable '%s' no declarada\n",s);
               break;
        case ERR_NOCABE:fprintf(stderr,"la variable '%s' ya no cabe en memoria\n",s);  
               // fila,columna de ':'
               break;
        case ERR_MAXTEMP:fprintf(stderr,"no hay espacio para variables temporales\n");
               // fila,columna da igual
               break;
        case ERR_RANGO:fprintf(stderr,"el segundo valor debe ser mayor o igual que el primero.");
               // fila,columna del segundo nÃºmero del rango
               break;
        case ERR_IFWHILE:fprintf(stderr,"la expresion del '%s' debe ser de tipo entero",s);
               break;
             
        case ERR_TOCHR:fprintf(stderr,"el argumento de '%s' debe ser entero.",s);
               break;

        case ERR_FALTAN: fprintf(stderr,"faltan indices\n");
               // fila,columna del id (si no hay ningÃºn Ã­ndice) o del Ãºltimo ']'
               break;
        case ERR_SOBRAN: fprintf(stderr,"sobran indices\n");
               // fila,columna del '[' si no es array, o de la ',' que sobra
               break;
        case ERR_INDICE_ENTERO: fprintf(stderr,"el indice de un array debe ser de tipo entero\n");
               // fila,columna del '[' si es el primero, o de la ',' inmediatamente anterior
               break;

        case ERR_ASIG: fprintf(stderr,"tipos incompatibles en la asignacion\n");
               // fila,columna del '='
               break;
        case ERR_OPIZQ: fprintf(stderr,"el operando de la izquierda de %s\n",s);
               // fila,columna del operador
               break;
        case ERR_OPDER: fprintf(stderr,"el operando de la derecha de %s\n",s);
               // fila,columna del operador
               break;
    }
    exit(-1);
}


void msgErrorOperador(int tipoesp,const char *op,int linea,int columna,int lado)
{
   string tipoEsp,mensaje;
   
   switch (tipoesp) {
     case ENTERO: tipoEsp="entero";
       break;
     case REAL: tipoEsp="real";
       break;
     case CHAR: tipoEsp="caracter";
       break;
     case NUMERICO: tipoEsp="entero o real";
       break;
   }

   mensaje= "'" ;
   mensaje += op ;
   mensaje += "' debe ser de tipo " ;
   mensaje += tipoEsp ;
   errorSemantico(lado,linea,columna,mensaje.c_str());
}

void msgError(int nerror,int nlin,int ncol,const char *s)
{
     switch (nerror) {
         case ERRLEXICO: fprintf(stderr,"Error lexico (%d,%d): caracter '%s' incorrecto\n",nlin,ncol,s);
            break;
         case ERRSINT: fprintf(stderr,"Error sintactico (%d,%d): en '%s'\n",nlin,ncol,s);
            break;
         case ERREOF: fprintf(stderr,"Error sintactico: fin de fichero inesperado\n");
            break;
         case ERRLEXEOF: fprintf(stderr,"Error lexico: fin de fichero inesperado\n");
            break;
        }
     exit(1);
}



int yyerror(char *s)
{
    extern int findefichero;  // de plp5.l
    if (findefichero) 
    {
       msgError(ERREOF,0,0,"");
    }
    else
    {  
       msgError(ERRSINT,nlin,ncol-strlen(yytext),yytext);
    }
    return 0;  // no llega, msgError hace exit
}

int main(int argc,char *argv[])
{
    FILE *fent;

    if (argc==2)
    {
        fent = fopen(argv[1],"rt");
        if (fent)
        {
            yyin = fent;
            yyparse();
            fclose(fent);
        }
        else
            fprintf(stderr,"No puedo abrir el fichero\n");
    }
    else
        fprintf(stderr,"Uso: ejemplo <nombre de fichero>\n");
}
