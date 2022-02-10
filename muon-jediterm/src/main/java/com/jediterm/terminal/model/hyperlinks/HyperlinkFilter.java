package com.jediterm.terminal.model.hyperlinks;


/**
 * @author traff
 */
public interface HyperlinkFilter {

  
  LinkResult apply(String line);
}
