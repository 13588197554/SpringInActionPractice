package guo.knights;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by guo on 20/2/2018.
 */
public class KnightMain {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("spring/knights.xml");    //加载Sprinig应用上下文
        Knight knight = context.getBean(Knight.class);                                       //获取knight bean
        knight.embarkOnQuest();                                                              //使用knight调用方法
        context.close();                                                                     //关闭应用上下文
    }
}
