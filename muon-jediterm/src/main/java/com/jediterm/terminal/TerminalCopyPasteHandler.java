package com.jediterm.terminal;


public interface TerminalCopyPasteHandler {
	void setContents(String text,
			boolean useSystemSelectionClipboardIfAvailable);

	String getContents(boolean useSystemSelectionClipboardIfAvailable);
}
