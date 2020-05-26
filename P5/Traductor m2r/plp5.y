/*------------------------------ plp5.y -------------------------------*/
%token prg var integer real caracter prn read_fun
%token if_con else_con
%token while_con toChr toInt
%token id numentero numreal
%token coma pyc dosp
%token lbra rbra pari pard cori cord
%token opas oprel opmul
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
int NuevaVariable();

const int ERRYADECL=1,ERRNODECL=2,ERRTIPOS=3,ERRNOSIMPLE=4,ERRNOENTERO=5;

string lexema, s1, s2;
int cvar = 0;
int ctemp = 16000;
TablaSimbolos *tsa = new TablaSimbolos(NULL); 

%}

%%


S           :  prg id dosp BlVar Bloque { 
                                          cout << convert(cvar) + "\n" + $5.cod + "halt\n"; 
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
                      unsigned tmp = NuevaVariable();
                      unsigned tipo = $0.th == "ENTERO" ? ENTERO : REAL;
                      struct Simbolo s = {$3.lexema, tipo, tmp, unsigned(1)};
                      tsa->anyadir(s);
                    }
                    else{ errorSemantico(ERR_YADECL,nlin,ncol,$3.lexema); }
                  }
            | id  {
                    if(tsa->buscar($1.lexema) == NULL){
                        unsigned tmp = NuevaVariable();
                        unsigned tipo = $0.th == "ENTERO" ? ENTERO : REAL;
                        struct Simbolo s = {$1.lexema, tipo, tmp, unsigned(1)};
                        tsa->anyadir(s);
                    }
                    else{ errorSemantico(ERR_YADECL,nlin,ncol,$1.lexema); }
                  }   

SeqInstr    : SeqInstr pyc { $$.nlin = ctemp; } Instr { $$.cod = $1.cod + $4.cod; ctemp = $3.nlin; }
            | { $$.nlin = ctemp; } Instr { $$.cod = $2.cod; ctemp = $1.nlin;}
            ;

Instr       : prn   { lexema = $1.lexema; $$.cod = lexema == "print" ? "" : "wrl\n"; } 
              Expr  {
                      if($3.tipo == ENTERO){
                        $$.cod = $3.cod + "wri " + convert($3.dir) + "\n" + $2.cod;
                      }
                      else{
                        $$.cod = $3.cod + "wrr " + convert($3.dir) + "\n" + $2.cod;
                      }
                    }
            ;

Expr        : Esimple   { $$.cod = $1.cod; $$.tipo = $1.tipo; $$.dir = $1.dir;} 
            ;

Esimple     : Term      { $$.cod = $1.cod; $$.tipo = $1.tipo; $$.dir = $1.dir; }
            ;

Term        : Factor    { $$.cod = $1.cod; $$.tipo = $1.tipo; $$.dir = $1.dir; }
            ;

Factor      : numentero {
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

int NuevaVariable(){
  int temp = cvar;
  cvar++;
  return temp;
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
