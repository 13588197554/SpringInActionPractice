package guo.knights;

import java.io.PrintStream;

/**
 * Created by guo on 20/2/2018.
 */
public class SlayDragonQuest implements Quest {
    private PrintStream stream;
    public SlayDragonQuest(PrintStream stream) {
        this.stream = stream;
    }
    @Override
    public void embark() {
        stream.println("Embarking on quest to slay the dragon!!,顺便还可以学英语，一举两得。");

    }
}
