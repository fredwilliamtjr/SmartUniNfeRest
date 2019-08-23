# SmartUniNfeRest
Servidor Rest em JavaFx com TrayIcon, Spring Boot para fazer a interface entre os arquivos enviados e retornados para o UniNfe!

Alguns exemplos de url:

Raiz do serviço:
GET http://localhost:6060/api/

Grava Certificado na pasta do UniNfe e devolve o caminho, o arquivo PFX em bytes vai no body do post!
POST http://localhost:6060/api/gravarCertficado/06199799000173/06199799/

Envia um arquivo para pasta geral, são 3 parâmetros, nome do arquivo que está sendo enviado, nome do arquivo com resposta de sucesso, nome do arquivo com resposta de erro, o arquivo vai no body do post!
POST http://localhost:6060/api/gravarArquivoGeral/uninfe-alt-con.xml/uninfe-ret-alt-con.xml/uninfe-ret-alt-con.err/
POST http://localhost:6060/api/gravarArquivoGeral/uninfe-cons-inf.xml/uninfe-ret-cons-inf.xml/uninfe-ret-cons-inf.err/

Busca o arquivo uniNfeConfig de uma empresa/servico especifico, são 2 parâmetros cnpj e serviço
GET http://localhost:6060/api/uniNfeConfig/06199799000173/nfse

Busca o arquivo uniNfeEmpresa
GET http://localhost:6060/api/uniNfeEmpresa

Envia arquivo para pasta da empresa, são 5 parâmetros, CNPJ da empresa, nome da pasta serviço, nome do arquivo que está sendo enviado, nome do arquivo com resposta de sucesso, nome do arquivo com resposta de erro, o arquivo vai no body do post!
POST http://localhost:6060/api/gravarArquivoEmpresa/40251563000177/nfse/40251563000177-20000-env-loterps.xml/40251563000177-20000-ret-loterps.xml/40251563000177-20000-ret-loterps.err/

Manipula o serviço do UniNfe
GET http://localhost:6060/api/servivo/status
GET http://localhost:6060/api/servivo/iniciar
GET http://localhost:6060/api/servivo/parar


Todas as requisições precisam enviar a autenticação Basic, o usuário e senha ficam registrados em: src\main\java\br\com\smartfull\Main.java

www.smartfull.com.br
fred@smartfull.com.br
