package com.guo.soundsystem;

/**
 * Created by guo on 21/2/2018.
 */
public class BlankDisc implements CompactDisc {
    private String title;
    private String artist;

    public BlankDisc(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    @Override
    public void play() {
        System.out.println("Playing " + title + " by " + artist);
    }
}
