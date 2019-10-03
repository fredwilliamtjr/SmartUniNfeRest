package br.com.smartfull;

import br.com.smartfull.controllers.HostServicesProvider;
import br.com.smartfull.rest.ApiRestController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.imageio.ImageIO;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootApplication
public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static String NOME = "Smart UniNfe Rest";
    public static String DATA_VERSAO = LocalDateTime.of(2019, Month.OCTOBER, 3, 16, 30, 0).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    public static String USUARIO_REST = "123";
    public static String SENHA_REST = "123";
    public static String DIRETORIO_BASE_UNINFE = "C:/Unimake/UniNFe/";
    public static String DIRETORIO_GERAL_UNINFE = "C:/Unimake/UniNFe/Geral/";
    public static String DIRETORIO_GERAL_RETORNO_UNINFE = "C:/Unimake/UniNFe/Geral/Retorno/";
    public static int NUMERO_MAXIMO_TENTATIVAS_RETORNO_UNINFE = 20;
    public static int TEMPO_ENTRE_TENTATIVAS_RETORNO_UNINFE = 500;

    private ConfigurableApplicationContext springContext;
    private Parent rootNode;

    private java.awt.SystemTray tray = null;
    private java.awt.TrayIcon trayIcon;
    private java.awt.Image iconeTray = null;
    private Image icone = null;

    public static void carregaConstantesArquivo(){
        if (new File("constantes.cfg").exists()) {
            try {
                List<String> readLines = FileUtils.readLines(new File("constantes.cfg"), StandardCharsets.UTF_8);
                if (!readLines.isEmpty()) {
                    //NOME = readLines.get(0);
                    //DATA_VERSAO = readLines.get(1);
                    USUARIO_REST = readLines.get(2);
                    SENHA_REST = readLines.get(3);
                    DIRETORIO_BASE_UNINFE = readLines.get(4);
                    DIRETORIO_GERAL_UNINFE = readLines.get(5);
                    DIRETORIO_GERAL_RETORNO_UNINFE = readLines.get(6);
                    NUMERO_MAXIMO_TENTATIVAS_RETORNO_UNINFE = Integer.parseInt(readLines.get(7));
                    TEMPO_ENTRE_TENTATIVAS_RETORNO_UNINFE = Integer.parseInt(readLines.get(8));
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }

        }
    }

    public static void main(final String[] args) {
        carregaConstantesArquivo();
        Application.launch(args);
    }

    @Override
    public void init() throws Exception {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
        builder.headless(false);
        springContext = builder.run();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        rootNode = fxmlLoader.load();
    }

    @Override
    public void start(Stage stage) throws Exception {

        HostServicesProvider.INSTANCE.init(getHostServices());

        this.stage = stage;
        stage.setTitle(Main.NOME.concat(" - ").concat(Main.DATA_VERSAO));
        stage.setScene(new Scene(rootNode));
        stage.show();

        icone = new Image("/fxml/l.png");
        iconeTray = ImageIO.read(getClass().getResource("/fxml/l.png")).getScaledInstance(16, 16, 16);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        stage.hide();
                    }
                });
            }
        });

        Platform.setImplicitExit(false);
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);

        Thread.sleep(500);
        stage.hide();

    }

    @Override
    public void stop() throws Exception {
        springContext.close();
    }


    private Stage stage;

    private void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
        }
    }

    private void addAppToTray() {
        try {
            java.awt.Toolkit.getDefaultToolkit();

            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("Sistema operacional nao suporta system tray!");
            }

            tray = java.awt.SystemTray.getSystemTray();
            trayIcon = new java.awt.TrayIcon(iconeTray);

            trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

            java.awt.MenuItem openItem = new java.awt.MenuItem("Maximizar " + Main.NOME);
            openItem.addActionListener(event -> Platform.runLater(this::showStage));

            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);

            java.awt.MenuItem exitItem = new java.awt.MenuItem("Sair");
            exitItem.addActionListener(event -> {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        Platform.exit();
                        System.exit(0);

                    }
                });

            });

            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            MouseMotionListener mml = new MouseMotionListener() {
                public void mouseDragged(MouseEvent e) {
                }

                public void mouseMoved(MouseEvent e) {

                    trayIcon.setToolTip(Main.NOME);
                }
            };
            trayIcon.addMouseMotionListener(mml);

            tray.add(trayIcon);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar system tray!");
        }
    }

}

