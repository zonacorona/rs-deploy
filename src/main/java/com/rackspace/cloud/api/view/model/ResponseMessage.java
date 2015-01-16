package com.rackspace.cloud.api.view.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResponseMessage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6127001252040653014L;
	private List<String> messages;

	public ResponseMessage(){
	    this.messages=new ArrayList<String>();	
	}
	
	public ResponseMessage(List<String> messages){
	    this.messages=messages;	
	}
	
	public List<String> getMessages() {
		return messages;
	}
	
	public void addAMessage(String message){
		this.messages.add(message);
	}
	
	public String toString(){
		StringBuffer retVal=new StringBuffer("");
		retVal.append("[messages:");
		
		if(this.messages.size()>0){
			for(String aMessage:this.messages){
				retVal.append("{");
				retVal.append(aMessage);
				retVal.append("},");
			}
		}
		else{
			retVal.append("{}");
		}
		retVal.append("]");
		return retVal.toString();
	}

}
