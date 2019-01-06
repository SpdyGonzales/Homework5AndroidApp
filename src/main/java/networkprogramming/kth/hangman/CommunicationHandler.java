package networkprogramming.kth.hangman;

import networkprogramming.kth.hangman.common.Message;

public interface CommunicationHandler {

    public void updateView(Message mes);
    public void quitGame();
}
