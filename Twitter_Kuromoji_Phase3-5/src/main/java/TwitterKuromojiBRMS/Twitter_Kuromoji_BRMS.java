package TwitterKuromojiBRMS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Iterator;

import org.atilika.kuromoji.Tokenizer;
import org.drools.core.ObjectFilter;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

 
public class Twitter_Kuromoji_BRMS {
	
	public static void main(String[] args) {
		
		try{
		
		/*
		 * ルールエンジンの設定。KieSerivceを作って、KieContainerを作って、そこからKieSessionを作ります。
		 * resource/META-INF内にあるkmodule.xmlで設定しているkSessionの名前をkieSessionを作るときに指定します。
		 * kmoduleにかかれているのは、resourceディレクトリ以下にあるrules というパッケージの中にあるルールを全部使います　ということです。
		 */
        KieServices ks = KieServices.Factory.get();
	    KieContainer kContainer = ks.getKieClasspathContainer();
    	KieSession kSession = kContainer.newKieSession("ksession-rules");
    	KieRuntimeLogger audit = ks.getLoggers().newFileLogger(kSession, "logs/audit");
    	
    	/*
    	 * 1TL だけ読み込む設定です。TLをコピーしてTL.txtにペーストしてください。
    	 */
    	File fileIn = new File("src/main/resources/TL.txt");
    	FileReader fileReader = new FileReader(fileIn);
    	BufferedReader br = new BufferedReader(fileReader);
    	
    	/*
    	 * kuromojiが使う辞書を指定しています。
    	 * ルールが実行し終わったら、この辞書に連結された単語を書き込みます。
    	 */
		File fileOut = new File("src/main/resources/kuromoji-dic.txt");
		FileWriter fileWriter = new FileWriter(fileOut, true);
		BufferedWriter bw = new BufferedWriter(fileWriter);
		
		/*
		 * kuromoji を指定の辞書を使ってインスタンス化します。
		 */
		Tokenizer tokenizer = Tokenizer.builder().userDictionary("src/main/resources/kuromoji-dic.txt").build();
    	
		/*
		 * TL.txtから文字列を読み込みます。
		 */
		String TL="";
    	String strIn;
    	while ((strIn = br.readLine()) != null){
    		TL = TL + strIn;
    	}
    	br.close();
    	fileReader.close();
//    	System.out.println(TL);
    	
    	/*
    	 * TwitterOneLineというClassを、読み込んだ文字列を使ってインスタンス化します。
    	 * これをルールエンジンにinsertします。
    	 * インスタンス化されたkuromojiもinsertします。
    	 * このプログラムではSTREAMは使いません。
    	 */
   		TwitterOneLine tol = new TwitterOneLine(TL);   	
    	kSession.insert(tol);
    	kSession.insert(tokenizer);
    	
    	/*
    	 * ルールエンジンを実行させます。
    	 * 戻り値はルールが実行された回数です。
    	 */
    	int firedNumber = kSession.fireAllRules();
    	System.out.println("ルール実行回数 : " + firedNumber);
    	
    	
    	 /*
    	  * ルールエンジンで実行された結果を取り出します。　フェーズ4。
    	  * 通常はルールを呼び出すプログラムで取り出すFactをinsertしておくので、それを参照すれば結果は取り出せます。
    	  * 今回はルールの中でFactを作っていますので、FactHandleのCollectionをルールエンジンから取得して、それを順次取り出す方法になります。
    	  * 取り出すFactHandleはInnerクラスだけを指定しています。
    	  */
		Collection<FactHandle> FactHandleCollection = kSession.getFactHandles(new ObjectFilter() {
			public boolean accept(Object object) {
				return object instanceof Inner;
			}
			});
			
		for (Iterator<FactHandle> i = FactHandleCollection.iterator(); i.hasNext();){
			FactHandle f = (FactHandle)i.next();
			Inner inner = (Inner)kSession.getObject(f);

			String str = null;
			
			/*
			 * 連結された名詞のみを取り出すとき、下記のif節のコメントアウトを解除してください。フェーズ4。
			 * コメントアウトされている状態では、全てのInner Classを取り出します。
			 * kuromoji の辞書形式にフォーマットを整えて文字列を作成します。
			 * "読み" は使わないことを想定しるので、 "*" としています。
			 */
//			if (inner.get品詞().equals("名詞,連結")) {  // フェーズ4でコメントアウトを解除してください。
			str = inner.get内容()+ ","+inner.get内容()+",*," + inner.get品詞() + "\n";
//			}  // フェーズ4でコメントアウトを解除してください。

			/*
			 * kuromojiの辞書に追加します。
			 */
			if (str != null) {
				bw.write(str);
			}
		}
		
    	bw.close();
    	fileWriter.close();
		audit.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
