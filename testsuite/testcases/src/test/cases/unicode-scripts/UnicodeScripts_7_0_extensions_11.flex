%%

%unicode 7.0
%public
%class UnicodeScripts_7_0_extensions_11

%type int
%standalone

%include ../../resources/common-unicode-all-enumerated-property-defined-values-only-java

%%

<<EOF>> { printOutput(); return 1; }
\p{Script_Extensions:Oriya} { setCurCharPropertyValue("Script_Extensions:Oriya"); }
[^] { }
