package com.guo;

import guo.knights.BraveKnight;
import guo.knights.Quest;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 * Created by guo on 20/2/2018.
 */
public class BraveKnightTest {
    @Test
    public void knightShouldEmbarkQuest() {
        Quest mockQuest = mock(Quest.class);               //创建mock Quest
        BraveKnight knight = new BraveKnight(mockQuest);   //注入mock Quest；
        knight.embarkOnQuest();
        verify(mockQuest,times(1)).embark();
    }
}
