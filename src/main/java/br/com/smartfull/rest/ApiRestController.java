package br.com.smartfull.rest;

import br.com.smartfull.Main;
import br.com.smartfull.utils.CNP;
import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class ApiRestController {

    private static final Logger log = LoggerFactory.getLogger(ApiRestController.class);

    @RequestMapping("")
    public ResponseEntity<Resposta> raiz() {
        log.info("raiz()");
        String texto = Main.NOME.concat(" - ").concat(Main.DATA_VERSAO).concat(" - Executando!");
        log.info(texto.concat(" - ").concat(HttpStatus.OK.toString()));
        return ResponseEntity.status(HttpStatus.OK).body(new Resposta(String.valueOf(HttpStatus.OK.value()), texto));
    }

    @RequestMapping(value = "/gravarCertficado/{cnpj}/{senha}", method = RequestMethod.POST)
    public ResponseEntity<Resposta> gravarCertficado(@RequestBody byte[] pfx, @PathVariable String cnpj, @PathVariable String senha) {
        log.info("gravarCertficado() : " + cnpj + ", " + senha);

        try {
            cnpj = CNP.removeMascara(cnpj);
            if (!CNP.isValidCNPJ(cnpj)) {
                String texto = "CNPJ ivalido!";
                log.error(texto.concat(" - ").concat(HttpStatus.CONFLICT.toString()));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Resposta(String.valueOf(HttpStatus.CONFLICT.value()), texto));
            }
            Certificado certificadoPfxBytes = CertificadoService.certificadoPfxBytes(pfx, senha);
            String cnpjCpf = certificadoPfxBytes.getCnpjCpf();
            if (!cnpjCpf.equals(cnpj)) {
                String texto = " - CNPJ informado não é o mesmo do certificado enviado!";
                log.error(texto.concat(" - ").concat(HttpStatus.CONFLICT.toString()));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Resposta(String.valueOf(HttpStatus.CONFLICT.value()), texto));
            }
            if (!certificadoPfxBytes.isValido()) {
                String texto = "Certifixado expirado!";
                log.error(texto.concat(" - ").concat(HttpStatus.CONFLICT.toString()));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Resposta(String.valueOf(HttpStatus.CONFLICT.value()), texto));
            }
            String caminhoCertificado = Main.DIRETORIO_BASE_UNINFE.concat(cnpj).concat("/certificado-").concat(cnpj).concat("-").concat(senha).concat(".pfx");
            FileUtils.writeByteArrayToFile(new File(caminhoCertificado), pfx, false);
            log.info(caminhoCertificado.concat(" - ").concat(HttpStatus.OK.toString()));
            return ResponseEntity.status(HttpStatus.OK).body(new Resposta(String.valueOf(HttpStatus.OK.value()), caminhoCertificado));
        } catch (CertificadoException e) {
            String texto = "Erro ao ler certificado enviado : " + e.getMessage();
            log.error(texto.concat(" - ").concat(HttpStatus.INTERNAL_SERVER_ERROR.toString()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), texto));
        } catch (IOException e) {
            String texto = "Erro ao gravar certificado enviado : " + e.getMessage();
            log.error(texto.concat(" - ").concat(HttpStatus.INTERNAL_SERVER_ERROR.toString()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), texto));
        }
    }

    @RequestMapping(value = "/gravarArquivoGeral/{nomeArquivoEnviado}/{nomeArquivoRetornoSucesso}/{nomeArquivoRetornoErro}", method = RequestMethod.POST)
    public ResponseEntity<Resposta> gravarArquivoGeral(@RequestBody String arquivo, @PathVariable String nomeArquivoEnviado, @PathVariable String nomeArquivoRetornoSucesso, @PathVariable String nomeArquivoRetornoErro) {
        log.info("gravarArquivoGeral() : " + nomeArquivoEnviado);

        try {
            String caminhoArquivoEnvio = Main.DIRETORIO_GERAL_UNINFE.concat(nomeArquivoEnviado);
            String caminhoArquivoSucesso = Main.DIRETORIO_GERAL_RETORNO_UNINFE.concat(nomeArquivoRetornoSucesso);
            String caminhoArquivoErro = Main.DIRETORIO_GERAL_RETORNO_UNINFE.concat(nomeArquivoRetornoErro);
            FileUtils.write(new File(caminhoArquivoEnvio), arquivo, StandardCharsets.UTF_8, false);
            //AGUARDAR UM DOS ARQUIVOS DE RETORNO DO ENVIO
            int quantidadeTentativasEnvio = 1;
            while (!new File(caminhoArquivoSucesso).exists() && !new File(caminhoArquivoErro).exists() && quantidadeTentativasEnvio <= Main.NUMERO_MAXIMO_TENTATIVAS_RETORNO_UNINFE) {
                String texto = "Aguardando arquivos de retorno : " + LocalDateTime.now() + ", Tentativa " + quantidadeTentativasEnvio;
                log.info(texto);
                quantidadeTentativasEnvio++;
                Thread.sleep(Main.TEMPO_ENTRE_TENTATIVAS_RETORNO_UNINFE);
            }
            //ABORTA CADO TENHA PASSADO DO NUMERO MAXIMO DE TENTATIVAS DE AGUARDAR OS ARQUIVOS DE RETORNO DE ENVIO
            if (quantidadeTentativasEnvio >= Main.NUMERO_MAXIMO_TENTATIVAS_RETORNO_UNINFE) {
                String texto = "Alcançou o número máximo de tentativas na espera de retorno da ".concat(Main.NOME).concat("!");
                log.error(texto.concat(" - ").concat(HttpStatus.GATEWAY_TIMEOUT.toString()));
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(new Resposta(String.valueOf(HttpStatus.GATEWAY_TIMEOUT.value()), texto));
            }
            if (new File(caminhoArquivoSucesso).exists()) {
                String arquivoSucesso = FileUtils.readFileToString(new File(caminhoArquivoSucesso), StandardCharsets.UTF_8);
                if (!StringUtils.isEmpty(arquivoSucesso)) {
                    boolean deleteQuietly = FileUtils.deleteQuietly(new File(caminhoArquivoSucesso));
                    log.info(arquivoSucesso.concat(" - ").concat(HttpStatus.OK.toString()));
                    return ResponseEntity.status(HttpStatus.OK).body(new Resposta(String.valueOf(HttpStatus.OK.value()), arquivoSucesso));
                }else {
                    boolean deleteQuietly = FileUtils.deleteQuietly(new File(caminhoArquivoSucesso));
                    log.info(arquivoSucesso.concat(" - ").concat(HttpStatus.INTERNAL_SERVER_ERROR.toString()));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "WebService da prefeitura instável, aguardar alguns minutos!"));
                }
            } else {
                String arquivoErro = FileUtils.readFileToString(new File(caminhoArquivoErro), StandardCharsets.UTF_8);
                boolean deleteQuietly = FileUtils.deleteQuietly(new File(caminhoArquivoErro));
                log.error(arquivoErro.concat(" - ").concat(HttpStatus.INTERNAL_SERVER_ERROR.toString()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), arquivoErro));
            }
        } catch (IOException | InterruptedException e) {
            String texto = "Erro ao gravar arquivo enviado : " + e.getMessage();
            log.error(texto.concat(" - ").concat(HttpStatus.INTERNAL_SERVER_ERROR.toString()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), texto));
        }
    }

    @RequestMapping(value = "/gravarArquivoEmpresa/{cnpj}/{servico}/{nomeArquivoEnviado}/{nomeArquivoRetornoSucesso}/{nomeArquivoRetornoErro}", method = RequestMethod.POST)
    public ResponseEntity<Resposta> gravarArquivoEmpresa(@RequestBody String arquivo, @PathVariable String cnpj, @PathVariable String servico, @PathVariable String nomeArquivoEnviado, @PathVariable String nomeArquivoRetornoSucesso, @PathVariable String nomeArquivoRetornoErro) {
        log.info("gravarArquivoEmpresa() : " + cnpj + ", " + nomeArquivoEnviado);

        try {
            cnpj = CNP.removeMascara(cnpj);
            if (!CNP.isValidCNPJ(cnpj)) {
                String texto = "CNPJ ivalido!";
                log.error(texto.concat(" - ").concat(HttpStatus.CONFLICT.toString()));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Resposta(String.valueOf(HttpStatus.CONFLICT.value()), texto));
            }
            String caminhoDiretorioBaseEmpresa = Main.DIRETORIO_BASE_UNINFE.concat(cnpj).concat("/");
            String caminhoServicoEmpresa = caminhoDiretorioBaseEmpresa.concat(servico).concat("/");
            String caminhoServicoEnvioEmpresa = caminhoServicoEmpresa.concat("Envio/");
            String caminhoServicoRetornoEmpresa = caminhoServicoEmpresa.concat("Retorno/");
            String caminhoArquivoEnvioEmpresa = caminhoServicoEnvioEmpresa.concat(nomeArquivoEnviado);
            String caminhoArquivoRetornoSucessoEmpresa = caminhoServicoRetornoEmpresa.concat(nomeArquivoRetornoSucesso);
            String caminhoArquivoRetornoErroEmpresa = caminhoServicoRetornoEmpresa.concat(nomeArquivoRetornoErro);
            FileUtils.write(new File(caminhoArquivoEnvioEmpresa), arquivo, StandardCharsets.UTF_8, false);
            //AGUARDAR UM DOS ARQUIVOS DE RETORNO DO ENVIO
            int quantidadeTentativasEnvio = 1;
            while (!new File(caminhoArquivoRetornoSucessoEmpresa).exists() && !new File(caminhoArquivoRetornoErroEmpresa).exists() && quantidadeTentativasEnvio <= Main.NUMERO_MAXIMO_TENTATIVAS_RETORNO_UNINFE) {
                String texto = "Aguardando arquivos de retorno : " + LocalDateTime.now() + ", Tentativa " + quantidadeTentativasEnvio;
                log.info(texto);
                quantidadeTentativasEnvio++;
                Thread.sleep(Main.TEMPO_ENTRE_TENTATIVAS_RETORNO_UNINFE);
            }
            //ABORTA CADO TENHA PASSADO DO NUMERO MAXIMO DE TENTATIVAS DE AGUARDAR OS ARQUIVOS DE RETORNO DE ENVIO
            if (quantidadeTentativasEnvio >= Main.NUMERO_MAXIMO_TENTATIVAS_RETORNO_UNINFE) {
                String texto = "Alcançou o número máximo de tentativas na espera de retorno da ".concat(Main.NOME).concat("!");
                log.error(texto.concat(" - ").concat(HttpStatus.GATEWAY_TIMEOUT.toString()));
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(new Resposta(String.valueOf(HttpStatus.GATEWAY_TIMEOUT.value()), texto));
            }
            if (new File(caminhoArquivoRetornoSucessoEmpresa).exists()) {
                String arquivoSucesso = FileUtils.readFileToString(new File(caminhoArquivoRetornoSucessoEmpresa), StandardCharsets.UTF_8);
                if (!StringUtils.isEmpty(arquivoSucesso)) {
                    boolean deleteQuietly = FileUtils.deleteQuietly(new File(caminhoArquivoRetornoSucessoEmpresa));
                    log.info(arquivoSucesso.concat(" - ").concat(HttpStatus.OK.toString()));
                    return ResponseEntity.status(HttpStatus.OK).body(new Resposta(String.valueOf(HttpStatus.OK.value()), arquivoSucesso));
                }else {
                    boolean deleteQuietly = FileUtils.deleteQuietly(new File(caminhoArquivoRetornoSucessoEmpresa));
                    log.info(arquivoSucesso.concat(" - ").concat(HttpStatus.INTERNAL_SERVER_ERROR.toString()));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "WebService da prefeitura instável, aguardar alguns minutos!"));
                }
            } else {
                String arquivoErro = FileUtils.readFileToString(new File(caminhoArquivoRetornoErroEmpresa), StandardCharsets.UTF_8);
                boolean deleteQuietly = FileUtils.deleteQuietly(new File(caminhoArquivoRetornoErroEmpresa));
                log.error(arquivoErro.concat(" - ").concat(HttpStatus.INTERNAL_SERVER_ERROR.toString()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), arquivoErro));
            }
        } catch (IOException | InterruptedException e) {
            String texto = "Erro ao gravar arquivo enviado : " + e.getMessage();
            log.error(texto.concat(" - ").concat(HttpStatus.INTERNAL_SERVER_ERROR.toString()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), texto));
        }
    }

    @RequestMapping(value = "/uniNfeEmpresa", method = RequestMethod.GET)
    public ResponseEntity<Resposta> uniNfeEmpresa() {

        log.info("uniNfeEmpresa()");
        try {
            String caminhoArquivoUniNfeEmpresa = Main.DIRETORIO_BASE_UNINFE.concat("UniNfeEmpresa.xml");
            String arquivoUniNfeEmpresa = FileUtils.readFileToString(new File(caminhoArquivoUniNfeEmpresa), StandardCharsets.UTF_8);
            log.info(arquivoUniNfeEmpresa.concat(" - ").concat(HttpStatus.OK.toString()));
            return ResponseEntity.status(HttpStatus.OK).body(new Resposta(String.valueOf(HttpStatus.OK.value()), arquivoUniNfeEmpresa));
        } catch (IOException e) {
            String texto = e.getMessage();
            log.error(texto.concat(" - ").concat(HttpStatus.INTERNAL_SERVER_ERROR.toString()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), texto));
        }
    }

    @RequestMapping(value = "/uniNfeConfig/{cnpj}/{servico}", method = RequestMethod.GET)
    public ResponseEntity<Resposta> uniNfeConfig(@PathVariable String cnpj, @PathVariable String servico) {
        log.info("uniNfeConfig() : " + cnpj + ", " + servico);

        try {
            String caminhoArquivoUniNfeConfig = Main.DIRETORIO_BASE_UNINFE.concat(cnpj).concat("/").concat(servico).concat("/UniNfeConfig.xml");
            String arquivoUniNfeConfig = FileUtils.readFileToString(new File(caminhoArquivoUniNfeConfig), StandardCharsets.UTF_8);
            log.info(arquivoUniNfeConfig.concat(" - ").concat(HttpStatus.OK.toString()));
            return ResponseEntity.status(HttpStatus.OK).body(new Resposta(String.valueOf(HttpStatus.OK.value()), arquivoUniNfeConfig));
        } catch (IOException e) {
            String texto = e.getMessage();
            log.error(texto.concat(" - ").concat(HttpStatus.INTERNAL_SERVER_ERROR.toString()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), texto));
        }
    }

    @RequestMapping(value = "/servivo/{acao}", method = RequestMethod.GET)
    public ResponseEntity<Resposta> servivo(@PathVariable String acao) throws IOException {
        log.info("servivo() : " + acao);

        String retorno;
        switch (acao) {
            case "iniciar":
                Runtime runtimeStart = Runtime.getRuntime();
                String[] scriptStart = {"cmd.exe", "/c", "sc", "start", "UniNFeServico"};
                Process processStart = runtimeStart.exec(scriptStart);
                StringBuilder stringBuilderStart = new StringBuilder();
                BufferedReader lineReaderStart = new BufferedReader(new InputStreamReader(processStart.getInputStream()));
                lineReaderStart.lines().forEach(linha -> stringBuilderStart.append(linha).append(System.getProperty("line.separator")));
                BufferedReader errorReaderStart = new BufferedReader(new InputStreamReader(processStart.getErrorStream()));
                errorReaderStart.lines().forEach(linha -> stringBuilderStart.append(linha).append(System.getProperty("line.separator")));
                retorno = stringBuilderStart.toString();
                break;
            case "parar":
                Runtime runtimeStop = Runtime.getRuntime();
                String[] scriptStop = {"cmd.exe", "/c", "sc", "stop", "UniNFeServico"};
                Process processStop = runtimeStop.exec(scriptStop);
                StringBuilder stringBuilderStop = new StringBuilder();
                BufferedReader lineReaderStop = new BufferedReader(new InputStreamReader(processStop.getInputStream()));
                lineReaderStop.lines().forEach(linha -> stringBuilderStop.append(linha).append(System.getProperty("line.separator")));
                BufferedReader errorReaderStop = new BufferedReader(new InputStreamReader(processStop.getErrorStream()));
                errorReaderStop.lines().forEach(linha -> stringBuilderStop.append(linha).append(System.getProperty("line.separator")));
                retorno = stringBuilderStop.toString();
                break;
            case "status":
                Runtime runtimeStatus = Runtime.getRuntime();
                String[] scriptStatus = {"cmd.exe", "/c", "sc", "query", "UniNFeServico", "|", "find", "/C", "\"RUNNING\""};
                Process processStatus = runtimeStatus.exec(scriptStatus);
                StringBuilder stringBuilderStatus = new StringBuilder();
                BufferedReader lineReaderStatus = new BufferedReader(new InputStreamReader(processStatus.getInputStream()));
                lineReaderStatus.lines().forEach(linha -> stringBuilderStatus.append(linha).append(System.getProperty("line.separator")));
                BufferedReader errorReaderStatus = new BufferedReader(new InputStreamReader(processStatus.getErrorStream()));
                errorReaderStatus.lines().forEach(linha -> stringBuilderStatus.append(linha).append(System.getProperty("line.separator")));
                retorno = stringBuilderStatus.toString();
                break;
            default:
                retorno = "Ação não identificada!";
                break;
        }
        log.error(retorno.concat(" - ").concat(HttpStatus.OK.toString()));
        return ResponseEntity.status(HttpStatus.OK).body(new Resposta(String.valueOf(HttpStatus.OK.value()), retorno));
    }


}
