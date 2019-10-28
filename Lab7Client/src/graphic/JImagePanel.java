package graphic;

import javax.swing.*;
import java.awt.*;

/**
 * Created by azamat on 09.10.17.
 */
public class JImagePanel extends JPanel {
    private Image bgImage;

    JImagePanel(){
        super();
    }

    JImagePanel(boolean isDoubleBuffered){
        super(isDoubleBuffered);
    }

    JImagePanel(LayoutManager layout){
        super(layout);
    }

    JImagePanel(LayoutManager layout, boolean isDoubleBuffered){
        super(layout, isDoubleBuffered);
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        }
    }

    public void setBgImage(Image bgImage) {
        this.bgImage = bgImage;
    }

}
