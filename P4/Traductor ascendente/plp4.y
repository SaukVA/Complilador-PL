/*------------------------------ plp4.y -------------------------------*/
%token pari pard 
%token dosp asig pyc
%token lbra rbra
%token opas oprel opmul
%token clase fun integer real print
%token if_con else_con fi
%token id numentero numreal

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


const int ERRYADECL=1,ERRNODECL=2,ERRTIPOS=3,ERRNOSIMPLE=4,ERRNOENTERO=5;

string lexema, s1, s2;
TablaSimbolos *tsa = new TablaSimbolos(NULL); 

%}

%%


X    : { $$.th = ""; } S    
                            { 
                              cout << $2.trad << endl; 
                              int tk = yylex();
                              if (tk != 0) yyerror("");
                            }
     ;

S    : clase id lbra {lexema = $2.lexema; $$.th = $0.th == "" ? lexema : $0.th + "_" + lexema; } M rbra  
                            {
                              
                              lexema = $2.lexema;
                              struct Simbolo p1 = {lexema, CLASSFUN, $0.th + "_" + lexema};
                              if(!tsa->buscarAmbito(p1)){tsa->anyadir(p1);} 
                              else{errorSemantico(ERRYADECL, $2.lexema, nlin, ncol);}
                              tsa = new TablaSimbolos(tsa);
                              $$.trad = $0.th == "" ? "//class " + lexema + "\n" + $5.trad : "//class " + $0.th + "_" + lexema + "\n" + $5.trad;
                              tsa = tsa->padre;
                            }
     ;

M    : M { $$.th = $0.th; } SF { $$.trad = $1.trad + $3.trad;} 
     | { $$.trad = "";}
     ;

SF   : { $$.th = $0.th;} S { $$.trad = $2.trad;}
     | { $$.th = $0.th;} Fun { $$.trad = $2.trad; }
     ;

Fun  : fun id A lbra {lexema = $2.lexema; $$.th = $0.th + "_" + lexema;} M rbra
                            {
                              lexema = $2.lexema;
                              struct Simbolo p1 = {lexema, CLASSFUN, $0.th + "_" + lexema};
                              if(!tsa->buscarAmbito(p1)){tsa->anyadir(p1);} 
                              else{errorSemantico(ERRYADECL, $2.lexema, nlin, ncol);}
                              tsa = new TablaSimbolos(tsa);
                              $$.trad = "void " + $0.th + "_" + lexema + "(" + $3.trad + "){\n" + $6.trad + "\n}\n";
                              tsa = tsa->padre;
                            }

A    : A pyc { $$.th = ""; } DV   {$$.trad = $1.trad + "," + $4.trad;}
     | { $$.th = ""; } DV   { $$.trad = $2.trad; }
     ;

DV   : Tipo id  
                            {
                              if($0.th == ""){
                                $$.trad = $1.trad + $2.lexema;
                              }
                              else{
                                $$.trad = $1.trad + $0.th + $2.lexema;
                              }
                            }
     ;

Tipo : integer  { $$.trad = "int "; $$.tipo = ENTERO;}
     | real  { $$.trad = "float "; $$.tipo = REAL;}
     ;


%%

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
    if (findefichero) 
    {
       msgError(ERREOF,-1,-1,"");
    }
    else
    {  
       msgError(ERRSINT,nlin,ncol-strlen(yytext),yytext);
    }
    return 0;
}



void errorSemantico(int nerror,char *lexema,int fila,int columna)
{
    fprintf(stderr,"Error semantico (%d,%d): en '%s', ",fila,columna,lexema);
    switch (nerror) {
      case ERRYADECL: fprintf(stderr,"ya existe en este ambito\n");
         break;
      case ERRNODECL: fprintf(stderr,"no ha sido declarado\n");
         break;
      case ERRTIPOS: fprintf(stderr,"tipos incompatibles entero/real\n");
         break;
      case ERRNOSIMPLE: fprintf(stderr,"debe ser de tipo entero o real\n");
         break;
      case ERRNOENTERO: fprintf(stderr,"debe ser de tipo entero\n");
         break;
    }
    exit(-1);
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
