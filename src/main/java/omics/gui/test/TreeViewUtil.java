package omics.gui.test;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class TreeViewUtil
{
    public static TreeView<String> getTreeView()
    {
        TreeItem<String> depts = new TreeItem<>("Departments");

        // Add items to depts
        TreeItem<String> isDept = new TreeItem<>("IS");
        TreeItem<String> claimsDept = new TreeItem<>("Claims");
        TreeItem<String> underwritingDept = new TreeItem<>("Underwriting");
        depts.getChildren().addAll(isDept, claimsDept, underwritingDept);

        // Add employees for each dept
        isDept.getChildren().addAll(new TreeItem<>("Doug Dyer"),
                new TreeItem<>("Jim Beeson"),
                new TreeItem<>("Simon Ng"));
        claimsDept.getChildren().addAll(new TreeItem<>("Lael Boyd"),
                new TreeItem<>("Janet Biddle"));
        underwritingDept.getChildren().addAll(new TreeItem<>("Ken McEwen"),
                new TreeItem<>("Ken Mann"),
                new TreeItem<>("Lola Ng"));
        // Create a TreeView with depts as its root item
        return new TreeView<>(depts);
    }
}