package org.cinchapi.quest;

class TestAction extends QuestAction {

    @Override
    public void run() {
        System.out.println("Jeff Nelson is a cool guy");

    }

    @Override
    protected String getDescription() {
        return "this is just a dummy thing fasfas";
    }

}