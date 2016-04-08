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
	
	public volatile static Inner inner;
	
	
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
			
			/*
			 * 毎秒毎にいくつのFACTがルールエンジン上にあるか出力しています。
			 * FACTが多すぎると処理に時間がかかり、最後はOutOfMemoryError を出して死にます。
			 * 本番環境などではメモリ量の管理が必要です。 
			 */
//			System.out.println("EntryPoint Fact? " + entrypoint.getFactCount());
			System.out.println ("FACT# " + kSession.getFactCount() + "   Thread? " + threadA.getState()  + "   Count:" + loopCount);
			Thread.sleep(1000);
			loopCount ++;

			/*
			 * おおよそ10秒毎に、連結された名詞を辞書に書出します。
			 */
				if (loopCount % 10 == 0){  //おおよそ10秒毎に辞書に書出し
							
					Collection<FactHandle> FactHandleCollection = kSession.getFactHandles(new ObjectFilter() {
						public boolean accept(Object object) {
							return object instanceof Inner;
						}
						});
									
					for (Iterator i = FactHandleCollection.iterator(); i.hasNext();){
						FactHandle f = (FactHandle)i.next();
						inner = (Inner)kSession.getObject(f);
						
						/*
						 * ルールエンジン内で作成されたInnerクラスを、違うスレッドが参照しに行くので、Innerをsynchronizedさせます。
						 */
					synchronized(inner){

			/*
			 * kuromojiの辞書の制限で、"#", "," , "*" は特別な意味を持ちます。
			 * これらが文字列中に入っているとkuromoji のReload時にjava.lang.ArrayIndexOutOfBoundsExceptionがThrowされ、kuromojiのインスタンス化が失敗します。
			 * 文字列に上記文字が入っているものは書き込まないようにします。 			
			 */
						String str = null;
						if (!inner.is取込み済み() && 
							(inner.get品詞().equals("名詞,連結") && 
								(!(inner.get内容().matches(".*#.*")) || !(inner.get内容().matches(".*,.*")) || !(inner.get内容().matches(".*\\*.*"))))) {
							str = inner.get内容()+ ","+inner.get内容()+",*," + "連結名詞\n";}

						if (str != null) {
							logger.info(str);
							fileWriter.write(str);
						}						
						/*
						 * 取込み済みのInnerに印をつけて、他で使用することを考えています。
						 */
							inner.set取込み済み(true);
							kSession.update(f, inner);
						
						} //synchronized(inner)
					
					} // for
					fileWriter.flush();
					
				/*
				 * 辞書に文字列を書き込んだら、新しい辞書でkuromojiを再度インスタンス化します。	
				 */
				tokenizer = Tokenizer.builder().userDictionary("src/main/resources/kuromoji-dic.txt").build();
				logger.info("tokenizer reloaded");
	
			} //if
			
			} // while
				

//		audit.close();

		fileWriter.close();
		System.exit(10);
		
		}
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
	logger.info("Start Twitter BRMS");

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
	