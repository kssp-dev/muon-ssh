package muon.app.ui.components.session;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.dnd.Autoscroll;

//http://www.java2s.com/Code/Java/Swing-JFC/DnDdraganddropJTreecode.htm
public class AutoScrollingJTree extends JTree implements Autoscroll {
    private final int margin = 12;

    public AutoScrollingJTree() {
        super();
    }

    public AutoScrollingJTree(TreeModel model) {
        super(model);
    }

    public void autoscroll(Point p) {
        int realrow = getRowForLocation(p.x, p.y);
        Rectangle outer = getBounds();
        realrow = (p.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1
                : realrow < getRowCount() - 1 ? realrow + 1 : realrow);
        scrollRowToVisible(realrow);
    }

    public Insets getAutoscrollInsets() {
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();
        return new Insets(inner.y - outer.y + margin, inner.x - outer.x + margin,
                outer.height - inner.height - inner.y + outer.y + margin,
                outer.width - inner.width - inner.x + outer.x + margin);
    }

}
