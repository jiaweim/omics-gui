package omics.gui.control;

import ch.qos.logback.core.OutputStreamAppender;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 12 Oct 2018, 10:49 AM
 */
public class TextAreaAppender<E> extends OutputStreamAppender<E>
{
    private static final DelegateOutputStream DELEGATE_OUTPUT_STREAM = new DelegateOutputStream(null);

    @Override
    public void start()
    {
        setOutputStream(DELEGATE_OUTPUT_STREAM);
        super.start();
    }

    public static void setDelegateOutputStream(OutputStream outputStream)
    {
        DELEGATE_OUTPUT_STREAM.setOutputStream(outputStream);
    }

    private static class DelegateOutputStream extends FilterOutputStream
    {

        /**
         * Creates an output stream filter built on top of the specified
         * underlying output stream.
         *
         * @param out the underlying output stream to be assigned to
         *            the field {@code this.out} for later use, or
         *            <code>null</code> if this instance is to be
         *            created without an underlying stream.
         */
        public DelegateOutputStream(OutputStream out)
        {
            super(new OutputStream()
            {
                @Override
                public void write(int b) throws IOException { }
            });
        }

        public void setOutputStream(OutputStream outputStream)
        {
            this.out = outputStream;
        }
    }

    public static class TextAreaOutputStream extends OutputStream
    {
        private final TextArea textArea;

        public TextAreaOutputStream(TextArea textArea)
        {
            this.textArea = textArea;
        }

        @Override
        public void write(int b)
        {
            Platform.runLater(() -> textArea.appendText(String.valueOf((char) b)));
        }

        @Override
        public void write(byte b[])
        {
            Platform.runLater(() -> textArea.appendText(new String(b)));
        }

        @Override
        public void write(byte b[], int off, int len)
        {
            Platform.runLater(() -> textArea.appendText(new String(b, off, len)));
        }
    }
}
