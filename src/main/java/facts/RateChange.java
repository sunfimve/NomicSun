package facts;

import enums.RateChangeType;
import agents.NomicAgent;

/**
 * @author Hanguang Sun
 *
 */
public class RateChange {
	public int addsub = 0;
	
	public float mod = 1;
	
	public RateChangeType RCType = RateChangeType.NONE;
	
	

	public RateChange(int as, float m, RateChangeType RT) {
		super();
		addsub = as;
		mod = m;
		RCType = RT;
	}
	
//	public int getRate()



	public RateChange(){
		super();
	}
	
}
