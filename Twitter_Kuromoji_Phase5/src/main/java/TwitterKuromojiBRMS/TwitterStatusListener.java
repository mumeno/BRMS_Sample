package TwitterKuromojiBRMS;

import java.util.List;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.kie.api.runtime.rule.EntryPoint;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TwitterStatusListener implements StatusListener {
	
	private EntryPoint ep;
	
	public TwitterStatusListener(EntryPoint ep){
		this.ep=ep;
	}

	public void onException(Exception arg0) {
		// TODO Auto-generated method stub

	}

	public void onDeletionNotice(StatusDeletionNotice arg0) {
		// TODO Auto-generated method stub

	}

	public void onScrubGeo(long arg0, long arg1) {
		// TODO Auto-generated method stub

	}

	public void onStallWarning(StallWarning arg0) {
		// TODO Auto-generated method stub

	}

	public void onStatus(Status status) {
		// TODO Auto-generated method stub
		ep.insert(status);
		
/*		if (status.getLang() =="ja"){
			Tokenizer tokenizer = Tokenizer.builder().build();
			List<Token> tokens = tokenizer.tokenize(status.getText());
			
		}
*/
		
	}

	public void onTrackLimitationNotice(int arg0) {
		// TODO Auto-generated method stub

	}

}
