package muon.app.ui.components.session.settings;

import java.awt.*;

public interface SessionTreeListener {
    default void treeValueChanged(Object nodeInfo) {
    }

    default void treeDoubleClick() {
    }

    default Window getWindow() {
        return null;
    }
}
