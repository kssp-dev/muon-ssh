/**
 *
 */
package muon.app.ui.components.settings;

import muon.app.App;
import util.FontUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @author subhro
 *
 */
public class FontItemRenderer extends JLabel implements ListCellRenderer<String> {

    /**
     *
     */
    public FontItemRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        System.out.println("Creating font in renderer: " + value);
        Font font = FontUtils.loadTerminalFont(value).deriveFont(Font.PLAIN, 14);
        setFont(font);
        setText(value);
        setBackground(isSelected ? App.SKIN.getAddressBarSelectionBackground() : App.SKIN.getSelectedTabColor());
        setForeground(isSelected ? App.SKIN.getDefaultSelectionForeground() : App.SKIN.getDefaultForeground());
        return this;
    }

}
