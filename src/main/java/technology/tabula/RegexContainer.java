package technology.tabula;

public class RegexContainer {
	
	private int containerType;
	
	private String upperLeft;
	private String upperRight;
	private String lowerLeft;
	private String lowerRight;
	
	private String top;
	private String bot;
	
	public RegexContainer(String ul, String ur, String ll, String lr) {
		
		// four string
		if(ul != null && ur != null
				&& ll != null && lr != null) {
			
			containerType = 4;
			
			upperLeft = ul;
			upperRight = ur;
			lowerLeft = ll;
			lowerRight = lr;
		}
		
		// two string
		else if((ul != null || ur != null)
				&& (ll != null || lr != null)) {			
			
			containerType = 2;
			
			if(ul != null)	top = ul;
			else top = ur;
			
			if(ll != null)	bot = ll;
			else bot = lr;
		}
		
		else
			
			containerType = 0;
		
    }
	
	public String getUpperLeft(){
		return upperLeft;		
	}
	
	public String getUpperRight(){
		return upperRight;		
	}
	
	public String getLowerLeft(){
		return lowerLeft;		
	}
	
	public String getLowerRight(){
		return lowerRight;		
	}
	
	public String getTop(){
		return top;
	}
	
	public String getBot(){
		return bot;
	}
	
	public int getType(){
		return containerType;
	}

}
