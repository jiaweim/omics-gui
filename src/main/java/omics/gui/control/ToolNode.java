package omics.gui.control;

import javafx.concurrent.Task;
import javafx.scene.Parent;
import org.controlsfx.control.TaskProgressView;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 01 Sep 2020, 12:42 PM
 */
public interface ToolNode
{
    /**
     * @return the tool name
     */
    String getToolName();

    /**
     * @return the root pane
     */
    Parent getRoot();

    /**
     * do some clean operations
     */
    void clear();

    /**
     * Add a task to the progress view
     *
     * @param task a task
     */
    default void viewProgress(Task<?> task)
    {
        TaskProgressView<Task<?>> taskProgressView = getTaskProgressView();
        if (taskProgressView != null) {
            taskProgressView.getTasks().add(task);
        }
    }

    TaskProgressView<Task<?>> getTaskProgressView();

    void setProgressView(TaskProgressView<Task<?>> progressView);
}
