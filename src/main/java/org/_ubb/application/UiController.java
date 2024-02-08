package org._ubb.application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org._ubb.config.ConfigLoader;
import org._ubb.config.NodeConfig;
import org._ubb.config.TorrentClientConfig;
import org._ubb.model.ClientNode;
import org._ubb.model.TorrentFileDto;
import org._ubb.network.NetworkHandler;
import org._ubb.service.FileTransferService;
import org._ubb.service.client.TorrentClient;
import org._ubb.utils.EncryptionUtils;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class UiController implements Initializable {

    private ClientNode clientNode;
    private FileTransferService fileTransferService;

    @FXML
    private TextField fileId;
    @FXML
    private TextField filePath;
    @FXML
    private Label peerIdInfo;
    @FXML
    private ListView<String> filesListView;

    public void downloadFile() {
        try {
            this.fileTransferService.downloadFile(fileId.getText());
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Failed");
            errorAlert.setContentText("Unable to download file");
            errorAlert.showAndWait();
            log.error(e.getMessage());
        }
        ;
    }

    public void uploadFile() {
        try {
            String filepath = filePath.getText();
            String identifier = EncryptionUtils.generateFileId(filepath);
            this.clientNode.addToDataStore(identifier, filepath);
            this.fileTransferService.uploadFile(identifier, filepath);
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setHeaderText("Success!");
            successAlert.setContentText("File upload successful");
            successAlert.showAndWait();
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Failed");
            errorAlert.setContentText("Unable to upload file: " + e.getMessage());
            errorAlert.showAndWait();
            log.error(e.getMessage());
        }
        ;

    }

    public void listFiles() {
        ObservableList<String> items = FXCollections.observableArrayList();
        try {
            List<TorrentFileDto> files = this.fileTransferService.getFiles();
            log.info(files.toString());
            for (var file : files) {
                items.add(
                        "File Hash: " + file.getFileHash() +" | " +
                        "File Name: " + file.getFileName() +" | " +
                        "File Type: " + file.getFileType() +" | " +
                        "Active Peers: " + file.getActivePeers().size()
                );

            }
        } catch (Exception e) {
            log.error("Failed to fetch files: " + e.getMessage());
        }
        if (!items.isEmpty()) {
            this.filesListView.setItems(items);
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NodeConfig config = new NodeConfig();
        TorrentClientConfig torrentClientConfig = new TorrentClientConfig();
        this.clientNode = new ClientNode("127.0.0.1", config.getLocalNodePort());
        NetworkHandler networkHandler = new NetworkHandler(config.getLocalNodePort());
        TorrentClient torrentClient = new TorrentClient(torrentClientConfig);
        this.fileTransferService = new FileTransferService(clientNode, networkHandler, torrentClient);
        this.fileTransferService.connectToNetwork();
        this.peerIdInfo.setText("Client peer id: " + clientNode.getClientId());

    }
}
