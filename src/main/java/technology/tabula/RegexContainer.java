/*
 * Todo:
 * 		Better type functionality
 * 		Multi-role strings, return what is needed based on type
 * 		Single string version
 * 
 * 		Try allocating array for globals
 */

package technology.tabula;

public class RegexContainer {
	
	private int containerType;	
	private String identifiers[];
	
	public RegexContainer(String list[]) {
		switch(list.length){
			case 1: { // single expression
				if(list[0] != null) {
					this.identifiers = new String[1];
					
					this.identifiers[0] = list[0];
					
					this.containerType = 1;
				}
				else {
					this.containerType = 0;
				}
				break;
			}
			
			case 2: { // two expressions
				if(list[0] != null && list[1] != null) {
					this.identifiers = new String[2];			
					
					this.identifiers[0] = list[0];
					this.identifiers[1] = list[1];
					
					this.containerType = 2;
				}		
				else {
					this.containerType = 0;
				}
				break;
			}
			
			case 4: { // four expressions
				// is this check needed?
				if(list[0] != null && list[1] != null
						&& list[2] != null && list[3] != null) {
					this.identifiers = new String[4];
					
					this.identifiers[0] = list[0];
					this.identifiers[1] = list[1];
					this.identifiers[2] = list[2];
					this.identifiers[3] = list[3];
					
					this.containerType = 4;
				}		
				else {			
					this.containerType = 0;
				}
				break;
			}	
			
			default: {
				this.containerType = 0;
				break;
			}
		}		
	}

	public String[] getIdentifiers(){
		return this.identifiers;
	}
	
	public int getType(){
		return this.containerType;
	}
}
