package br.com.smartfull.rest;

import br.com.smartfull.Main;
import br.com.smartfull.controllers.MainController;
import br.com.smartfull.utils.CNP;
import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class ApiRestController {

    private static final Logger log = LoggerFactory.getLogger(ApiRestController.class);

    @RequestMapping("")
    public Resposta raiz() {
        String texto = Main.NOME.concat(" - ").concat(Main.DATA_VERSAO).concat(" - Executando!");
        log.info(texto);
        return new Resposta(texto);
    }

    @RequestMapping(value = "/gravarCertficado/{cnpj}/{senha}", method = RequestMethod.POST)
    public ResponseEntity<Resposta> gravarCertficado(@RequestBody byte[] pfx, @PathVariable String cnpj, @PathVariable String senha) {
        try {
            cnpj = CNP.removeMascara(cnpj);
            if (!CNP.isValidCNPJ(cnpj)) {
                String texto = "CNPJ ivalido!";
                log.error(texto);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Resposta(texto));
            }
            Certificado certificadoPfxBytes = CertificadoService.certificadoPfxBytes(pfx, senha);
            String cnpjCpf = certificadoPfxBytes.getCnpjCpf();
            if (!cnpjCpf.equals(cnpj)) {
                String texto = "CNPJ informado não é o mesmo do certificado enviado!";
                log.error(texto);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Resposta(texto));
            }
            if (!certificadoPfxBytes.isValido()) {
                String texto = "Certifixado expirado!";
                log.error(texto);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Resposta(texto));
            }
            String caminhoCertificado = Main.DIRETORIO_BASE_UNINFE.concat(cnpj).concat("/certificado-").concat(cnpj).concat("-").concat(senha).concat(".pfx");
            FileUtils.writeByteArrayToFile(new File(caminhoCertificado), pfx, false);
            log.info(caminhoCertificado);
            return ResponseEntity.status(HttpStatus.OK).body(new Resposta(caminhoCertificado));
        } catch (CertificadoException e) {
            String texto = "Erro ao ler certificado enviado : " + e.getMessage();
            log.error(texto);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(texto));
        } catch (IOException e) {
            String texto = "Erro ao gravar certificado enviado : " + e.getMessage();
            log.error(texto);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(texto));
        }
    }

    @RequestMapping(value = "/gravarArquivoGeral/{nomeArquivoEnviado}/{nomeArquivoRetornoSucesso}/{nomeArquivoRetornoErro}", method = RequestMethod.POST)
    public ResponseEntity<Resposta> gravarArquivoGeral(@RequestBody String arquivo, @PathVariable String nomeArquivoEnviado, @PathVariable String nomeArquivoRetornoSucesso, @PathVariable String nomeArquivoRetornoErro) {
        try {
            String caminhoArquivoEnvio = Main.DIRETORIO_GERAL_UNINFE.concat(nomeArquivoEnviado);
            FileUtils.write(new File(caminhoArquivoEnvio), arquivo, StandardCharsets.UTF_8, false);
            String caminhoArquivoSucesso = Main.DIRETORIO_GERAL_RETORNO_UNINFE.concat(nomeArquivoRetornoSucesso);
            String caminhoArquivoErro = Main.DIRETORIO_GERAL_RETORNO_UNINFE.concat(nomeArquivoRetornoErro);
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
                String texto = "Alcançou o número máximo de tentativas na espera de retorno do UniNfe!";
                log.error(texto);
                return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(new Resposta(texto));
            }
            if (new File(caminhoArquivoSucesso).exists()) {
                String arquivoSucesso = FileUtils.readFileToString(new File(caminhoArquivoSucesso), StandardCharsets.UTF_8);
                boolean deleteQuietly = FileUtils.deleteQuietly(new File(caminhoArquivoSucesso));
                log.info(arquivoSucesso);
                return ResponseEntity.status(HttpStatus.OK).body(new Resposta(arquivoSucesso));
            } else {
                String arquivoErro = FileUtils.readFileToString(new File(caminhoArquivoErro), StandardCharsets.UTF_8);
                boolean deleteQuietly = FileUtils.deleteQuietly(new File(caminhoArquivoErro));
                log.error(arquivoErro);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(arquivoErro));
            }
        } catch (IOException | InterruptedException e) {
            String texto = "Erro ao gravar arquivo enviado : " + e.getMessage();
            log.error(texto);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(texto));
        }
    }

    @RequestMapping(value = "/gravarArquivoEmpresa/{cnpj}/{servico}/{nomeArquivoEnviado}/{nomeArquivoRetornoSucesso}/{nomeArquivoRetornoErro}", method = RequestMethod.POST)
    public ResponseEntity<Resposta> gravarArquivoEmpresa(@RequestBody String arquivo, @PathVariable String cnpj, @PathVariable String servico, @PathVariable String nomeArquivoEnviado, @PathVariable String nomeArquivoRetornoSucesso, @PathVariable String nomeArquivoRetornoErro) {
        try {
            cnpj = CNP.removeMascara(cnpj);
            if (!CNP.isValidCNPJ(cnpj)) {
                String texto = "CNPJ ivalido!";
                log.error(texto);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Resposta(texto));
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
                String texto = "Alcançou o número máximo de tentativas na espera de retorno do UniNfe!";
                log.error(texto);
                return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(new Resposta(texto));
            }
            if (new File(caminhoArquivoRetornoSucessoEmpresa).exists()) {
                String arquivoSucesso = FileUtils.readFileToString(new File(caminhoArquivoRetornoSucessoEmpresa), StandardCharsets.UTF_8);
                boolean deleteQuietly = FileUtils.deleteQuietly(new File(caminhoArquivoRetornoSucessoEmpresa));
                log.info(arquivoSucesso);
                return ResponseEntity.status(HttpStatus.OK).body(new Resposta(arquivoSucesso));
            } else {
                String arquivoErro = FileUtils.readFileToString(new File(caminhoArquivoRetornoErroEmpresa), StandardCharsets.UTF_8);
                boolean deleteQuietly = FileUtils.deleteQuietly(new File(caminhoArquivoRetornoErroEmpresa));
                log.error(arquivoErro);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(arquivoErro));
            }
        } catch (IOException | InterruptedException e) {
            String texto = "Erro ao gravar arquivo enviado : " + e.getMessage();
            log.error(texto);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Resposta(texto));
        }
    }

}
