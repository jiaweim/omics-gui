package omics.gui.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import omics.util.io.FilenameUtils;

import java.io.File;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 22 Jan 2018, 1:56 PM
 */
public class FastaDecoyController extends OmicsController
{
    public TextField text_input;
    public RadioButton button_rev;
    public RadioButton button_random;
    public CheckBox checkBox_concated;
    public ProgressBar progress_bar;
    public Label label_status;
    private File inputFile;
    private File outputFile;
    private Stage stage;
    private Task task;

    public void setStage(Stage stage)
    {
        this.stage = stage;
    }

    public void chooseInput(ActionEvent event)
    {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fasta File", "*.fasta"));

        this.inputFile = chooser.showOpenDialog(stage);
        if (inputFile != null) {
            this.text_input.setText(inputFile.getAbsolutePath());
            this.outputFile = new File(FilenameUtils.appendSuffix(inputFile.getAbsolutePath(), "_final"));
        }
    }

    public void startTask(ActionEvent event) throws Exception
    {
//        DecoyType taskType;
//        if (button_random.isSelected()) {
//            taskType = DecoyType.RANDOM;
//        } else
//            taskType = DecoyType.REVERSE;

        final boolean concated = checkBox_concated.isSelected();

//        task = new Task()
//        {
//            @Override
//            protected Void call() throws Exception
//            {
//                DoShuffleDB shuffleDB = new DoShuffleDB(inputFile, outputFile, taskType, concated, ProteinDB.REVERSE);
//                shuffleDB.progressProperty().addListener(evt -> updateProgress((Double) evt.getNewValue(), 1.0));
//                shuffleDB.messageProperty().addListener(evt -> updateMessage((String) evt.getNewValue()));
//                shuffleDB.exceptionProperty().addListener(evt -> setException((Throwable) evt.getNewValue()));
//
//                shuffleDB.start();
//                return null;
//            }
//        };

        progress_bar.progressProperty().bind(task.progressProperty());
        label_status.textProperty().bind(task.messageProperty());

        Thread thread = new Thread(task);
        thread.start();
    }

    public void cancelTask(ActionEvent event)
    {
        if (task != null)
            task.cancel();
    }
}
