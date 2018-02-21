package com.guo.soundsystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by guo on 21/2/2018.
 */
@Component
public class CDPlayer implements MediaPlayer {
    private CompactDisc cd;
    @Autowired
    public void setCd(CompactDisc cd) {
        this.cd = cd;
    }

    @Autowired
    public  CDPlayer(CompactDisc cd) {
        this.cd = cd;
    }
    @Override
    public void play() {
        cd.play();
    }
}
