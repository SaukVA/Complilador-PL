/*------------------------------ plp4.l -------------------------------*/
D    [0-9]
L    [a-zA-Z]
LD   [0-9a-zA-Z]

%{
#include <string.h> 
#include <string>
#include <iostream>

using namespace std;

#include "comun.h"
#include "plp4.tab.h"

int ncol = 1,
    nlin = 1;

int findefichero = 0;

int ret(int token);  

void msgError(int nerror, int nlin,int ncol,const char *s);

%}

%x COMENTARIO

%%
%{
/* codigo local */
%}

" "                {ncol++;}
[\t]               {ncol++;}
[\n]               {nlin++;ncol=1;}
"/*"                    {ncol += strlen(yytext);BEGIN(COMENTARIO);}
<COMENTARIO>[^/*\n]+    {ncol += strlen(yytext);}
<COMENTARIO>\n          {nlin++;ncol=1;}
<COMENTARIO><<EOF>>     {msgError(ERRLEXEOF,-1,-1,"");}
<COMENTARIO>"*/"        {ncol+=strlen(yytext); BEGIN(INITIAL);}
<COMENTARIO>[*/]        {ncol += strlen(yytext);}
"class"			   {return ret(clase);}
"fun"			   {return ret(fun);}
"int"			   {return ret(integer);}
"float"			   {return ret(real);}
"if"			   {return ret(if_con);}
"else"			   {return ret(else_con);}
"fi"			   {return ret(fi);}
"print"            {return ret(print);}            
{L}({LD})*         {return ret(id);}
{D}+               {return ret(numentero);}
{D}+(\.){D}+       {return ret(numreal);}
"("                {return ret(pari);}
")"                {return ret(pard);}
":"				   {return ret(dosp);}
"{"				   {return ret(lbra);}
"}"				   {return ret(rbra);}
"="				   {return ret(asig);}
";"                {return ret(pyc);}
"<"				   {return ret(oprel);}
"<="			   {return ret(oprel);}
">"				   {return ret(oprel);}
">="			   {return ret(oprel);}
"=="			   {return ret(oprel);}
"!="			   {return ret(oprel);}
"+"                {return ret(opas);}
"-"                {return ret(opas);}
"*"				   {return ret(opmul);}
"/"				   {return ret(opmul);}
.                  {msgError(ERRLEXICO,nlin,ncol,yytext);}

%%

int yywrap(void) {findefichero=1; return 1;} 


int ret(int token)
{
   yylval.lexema=strdup(yytext);
   yylval.nlin=nlin;
   yylval.ncol=ncol;
   ncol+=(strlen(yytext));
   return(token);
}
