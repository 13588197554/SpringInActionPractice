import com.guo.soundsystem.CompactDisc;
import com.guo.soundsystem.MediaPlayer;
import org.springframework.beans.factory.annotation.Autowired;


public class CDPlayer implements MediaPlayer {
  private CompactDisc compactDisc;

  @Autowired
  public void setCompactDisc(CompactDisc compactDisc) {
    this.compactDisc = compactDisc;
  }

  public void play() {
    compactDisc.play();
  }

}
