package com.guo.soundsystem;

import org.springframework.stereotype.Component;

/**
 * Created by guo on 21/2/2018.
 */
@Component
public class SgtPeppers implements CompactDisc {
    private String title = "Sgt. Pepper's Lonely Hearts Club Band";
    private String artist = "The Beatles";

    public void play() {
        System.out.println("Playing " + title + " by " + artist);
    }
}
