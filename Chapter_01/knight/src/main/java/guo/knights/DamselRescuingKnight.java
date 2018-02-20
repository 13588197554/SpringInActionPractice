package guo.knights;

/**
 * Created by guo on 20/2/2018.
 */
public class DamselRescuingKnight implements Knight {
    private RescueDamselQuest quest;

    public DamselRescuingKnight ( RescueDamselQuest quest) {
        //与RescueDamselQuest紧耦合
        this.quest = new RescueDamselQuest();
    }
    @Override
    public void embarkOnQuest() {
         quest.embark();
    }
}
