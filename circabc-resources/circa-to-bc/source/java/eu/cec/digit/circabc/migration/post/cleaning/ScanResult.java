/**
 * 
 */
package eu.cec.digit.circabc.migration.post.cleaning;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author beaurpi
 *
 */
public class ScanResult {

	private String userName;
	private NodeRef targetRef;
	private String targetName;
	private String actionInformation;
	
	/**
	 * 
	 */
	public ScanResult() {
		
	}
	
	/**
	 * 
	 */
	public ScanResult(String userName, NodeRef targetRef, String actionInformation, String targetName) {
		this.userName=userName;
		this.targetRef=targetRef;
		this.targetName=targetName;
		this.actionInformation=actionInformation;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the targetRef
	 */
	public NodeRef getTargetRef() {
		return targetRef;
	}

	/**
	 * @param targetRef the targetRef to set
	 */
	public void setTargetRef(NodeRef targetRef) {
		this.targetRef = targetRef;
	}

	/**
	 * @return the actionInformation
	 */
	public String getActionInformation() {
		return actionInformation;
	}

	/**
	 * @param actionInformation the actionInformation to set
	 */
	public void setActionInformation(String actionInformation) {
		this.actionInformation = actionInformation;
	}

	public void setTargetName(String string) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the targetName
	 */
	public String getTargetName() {
		return targetName;
	}

}
