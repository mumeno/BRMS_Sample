package TwitterKuromojiBRMS;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.atilika.kuromoji.Tokenizer;
import org.drools.core.ObjectFilter;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;


public class  Twitter_Kuromoji_BRMS  implements Runnable{
	
	private static Logger logger = LoggerFactory.getLogger(Twitter_Kuromoji_BRMS.class);
	static KieServices ks;
	static KieContainer kContainer;
	static KieSession kSession;
	static EntryPoint entrypoint;
	
//	public volatile static Inner inner;
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		try {
			
		/*
		 * Twitter4jから受ける部分とルールエンジンを別スレッドで立ち上げます。
		 */
		Twitter_Kuromoji_BRMS Twitter_Kuromoji_BRMS = new Twitter_Kuromoji_BRMS();
		Thread threadA = new Thread(Twitter_Kuromoji_BRMS);
		
		/*
		 * kuromojiの辞書の場所です。辞書に書き込みます。
		 */
		File file = new File("src/main/resources/kuromoji-dic.txt");
		FileWriter fileWriter = new FileWriter(file, true);
		
		
		Tokenizer tokenizer = null;
		FactHandle tokenizerHandle = null;
		
		long loopCount = 0;
		boolean running = true;

		/*
		 * keiSessionをスタートさせます。
		 */
		Twitter_Kuromoji_BRMS.StartKieSession();
	    
		/*
		 * KieSessionのaudit logを取る場合はここをコメントアウトを解除してください。
		 * ただし、すごい量のlogが出ます...
		 * main 最後の audit.close() もコメントアウトの解除をするのをお忘れなく。
		 */
		// KieRuntimeLogger audit = ks.getLoggers().newFileLogger(kSession, "audit");
		
		/*
		 * Thread をスタートさせます。
		 */
		threadA.start();

		/*
		 * 別Threadで動いているRule EngineのkSessionから情報を横取りします。
		 */
		
		while(running){
			/*
			 * RuleEngine 上でFactとして置くkuromoji のFactHandler がnullの場合、kuromoji の初期化を行います。
			 */
			if (tokenizerHandle == null){
			tokenizer = Tokenizer.builder().userDictionary("src/main/resources/kuromoji-dic.txt").build();
			tokenizerHandle = entrypoint.insert(tokenizer);
			System.out.println("kuromoji initialized.");
			}
		Thread.sleep(10000);

		} //while
		} //try
		catch (Exception E) {
			E.printStackTrace();
		}
		
		}	//main


synchronized public void StartKieSession(){
	ks = KieServices.Factory.get();
    kContainer = ks.getKieClasspathContainer();
    this.kSession = kContainer.newKieSession("ksession-rules");
    
	KieBaseConfiguration kConfig = ks.newKieBaseConfiguration();
	kConfig.setOption(EventProcessingOption.STREAM);    	
	entrypoint = kSession.getEntryPoint("TwitterEntryPoint");
	
	logger.info("EntryPoint \"TwitterEntryPoint\" created.");

}


public void run() {
	logger.info("Start Twitter_Kuromoji_BRMS");

	// Twiiter
	TwitterFactory factory = new TwitterFactory();
	Twitter twitter = factory.getSingleton();

	StatusListener listener = new TwitterStatusListener(this.entrypoint);
	TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
	twitterStream.addListener(listener);

	twitterStream.sample();
	
	this.kSession.fireUntilHalt();
	
} // void run end


} // class end
	