package guo.knights;

/**
 * Created by guo on 20/2/2018.
 */
public class BraveKnight implements Knight {
    public Quest quest;

    public BraveKnight(Quest quest) {          //Quest被注入进来
        this.quest = quest;
    }

    @Override
    public void embarkOnQuest() {
        quest.embark();
    }
}
