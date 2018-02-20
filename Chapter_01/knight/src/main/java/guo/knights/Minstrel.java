package guo.knights;

import java.io.PrintStream;

/**
 * Created by guo on 20/2/2018.
 * 咏游诗人，作为骑士的一个切面
 */
public class Minstrel {
    private PrintStream stream;

    public Minstrel(PrintStream stream) {
        this.stream = stream;
    }
    public void singBeforeQuest() {
        stream.println("Fa la la ,the Knight is so brabe");      //探险之前调用
    }
    public void singAfterQuest() {
        stream.println("Tee hee hhe,the brave knight " + "did embark on a quest");   //探险之后调用
    }
}
