package com.jediterm.terminal.model.hyperlinks;


/**
 * @author traff
 */
public class LinkInfo {
  private final Runnable myNavigateCallback;

  public LinkInfo( Runnable navigateCallback) {
    myNavigateCallback = navigateCallback;
  }

  public void navigate() {
    myNavigateCallback.run();
  }
}
