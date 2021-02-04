package omics.gui.test;


import omics.util.io.FileUtils;

import java.net.URL;

public class Test
{
    public static void main(String[] args)
    {

        URL resource = FileUtils.getResource("test");
        System.out.println(resource);
        System.out.println(Test.class.getClassLoader().getResource("main.fxml"));
    }
}
