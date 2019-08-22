# SmartUniNfeRest
Servidor Rest em JavaFx com TrayIcon, Spring Boot para fazer a interface entre os arquivos enviador e retornados para o UniNfe!

Alguns exemplos de url:

Raiz do serviço:
http://localhost:6060/api/

Grava Certificado na pasta do UniNfe e devolve o caminho, o arquivo PFX em bytes vai no body do post!
http://localhost:6060/api/gravarCertficado/06199799000173/06199799/

Envia um arquivo para pasta geral, são 3 parametros, nome do aruivo que esta sendo enviado, nome do arquivo com rsposta de sucesso, nome do arquivo com resposta de erro, o arquivo vai no body do post!
http://localhost:6060/api/gravarArquivoGeral/uninfe-alt-con.xml/uninfe-ret-alt-con.xml/uninfe-ret-alt-con.err/
http://localhost:6060/api/gravarArquivoGeral/uninfe-cons-inf.xml/uninfe-ret-cons-inf.xml/uninfe-ret-cons-inf.err/

Envia arquivo para pasta da empresa, são 5 parametros, CNPJ da empresa, nome da pasta servico, nome do aruivo que esta sendo enviado, nome do arquivo com rsposta de sucesso, nome do arquivo com resposta de erro, o arquivo vai no body do post!
http://localhost:6060/api/gravarArquivoEmpresa/40251563000177/nfse/40251563000177-20000-env-loterps.xml/40251563000177-20000-ret-loterps.xml/40251563000177-20000-ret-loterps.err/


www.smartfull.com.br

fred@smartfull.com.br
