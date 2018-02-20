package guo.knights;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by guo on 20/2/2018.
 */
public class KnightAopMain {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("spring/minstrel-AOP.xml");
       Knight knight = context.getBean(Knight.class);
       //Knight knight = (Knight) context.getBean("knight");
        knight.embarkOnQuest();
        context.close();
    }
}
