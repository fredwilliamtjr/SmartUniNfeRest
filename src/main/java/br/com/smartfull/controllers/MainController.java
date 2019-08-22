/*
    Maven-JavaFX-Package-Example
    Copyright (C) 2017-2018 Luca Bognolo

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.

 */
package br.com.smartfull.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Main controller class for the entire layout.
 */
@Component
public class MainController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    public static String textoLog = "";

    @FXML
    private Hyperlink hyperlink;

    @FXML
    private javafx.scene.control.TextArea textArea;

    @FXML
    private void openURL() {
        HostServicesProvider.INSTANCE.getHostServices().showDocument(hyperlink.getText());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        atualizaComponentes.setCycleCount(Timeline.INDEFINITE); // Executar indefinidamente.
        atualizaComponentes.play();
    }

    Timeline atualizaComponentes = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        try {
            StringBuilder stringBuilderTextoLog = new StringBuilder();
            List<String> readLines = FileUtils.readLines(new File("SmartUniNfeRest.log"), StandardCharsets.UTF_8);
            int tamnahoLog = readLines.size();
            if (tamnahoLog > 17) {
                int tamnahoLogMenos500 = tamnahoLog - 17;
                readLines = readLines.subList(tamnahoLogMenos500, tamnahoLog);
            }
            for (String linha : readLines) {
                stringBuilderTextoLog.append(linha).append(System.getProperty("line.separator"));
            }
            textArea.setText(stringBuilderTextoLog.toString());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }));


}
