package com.rackspace.cloud.api.jclouds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.rackspace.cloudloadbalancers.v1.CloudLoadBalancersApi;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.LoadBalancer;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.Node;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.UpdateNode;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.internal.BaseNode.Condition;
import org.jclouds.rackspace.cloudloadbalancers.v1.features.NodeApi;
import org.jclouds.rackspace.cloudloadbalancers.v1.predicates.LoadBalancerPredicates;
import org.json.JSONException;
import org.json.JSONObject;

import com.rackspace.cloud.api.DeployUtility;


public class JCloudsUtility{

	private static Logger log = Logger.getLogger(JCloudsUtility.class);  
	private static Properties propsFile;
	public static final String ZONE = "DFW";
	public static final String CONNECTION_TIME_ALARM_CRITERIA="if (metric['duration'] > 25000) { return new AlarmStatus(CRITICAL, "+
			"'HTTP request took more than 25000 milliseconds.');} if (metric['duration'] > 20000) {return new AlarmStatus(WARNING, "+
			"'HTTP request took more than 20000 milliseconds.');} return new AlarmStatus(OK, 'HTTP connection time is normal'); ";

	public static final String STATUS_CODE_ALARM_CRITERIA="if (metric['code'] regex '4[0-9][0-9]') {return new AlarmStatus(CRITICAL, "+
			"'HTTP server responding with 4xx status');} if (metric['code'] regex '5[0-9][0-9]') { return new AlarmStatus(CRITICAL, "+
			"'HTTP server responding with 5xx status');} return new AlarmStatus(OK, 'HTTP server is functioning normally');";
	public static final String CONNECTION_TIME_LABEL="Connection time";
	public static final String STATUS_CODE_LABEL="Status code";
	public static final String NOTIFICATION_PLAN="npTechnicalContactsEmail";

	public static final String CHECK_TYPE="remote.http";

	private static void loadPropsFile(HttpServletRequest request){
		String METHOD_NAME="loadPropsFile()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
			log.debug(METHOD_NAME+": (null==JCloudsUtility.propsFile)="+(null==JCloudsUtility.propsFile));
		}
		if(null==JCloudsUtility.propsFile){
			try {			
				//ServletContext ctx=super.getServletContext().getContext("/rax-autodeploy");
				//InputStream inny=ctx.getResourceAsStream("WEB-INF/props.properties");
				InputStream inny=JCloudsUtility.class.getClassLoader().getResourceAsStream("props.properties");
				String serverName=request.getServerName();
				if(log.isDebugEnabled()){
					log.debug("serverName="+serverName);
				}
				if(null!=serverName && serverName.toLowerCase().trim().contains("content-services")){
					File theFile=new File("/home/docs/DeployWars/props.properties");
					if(log.isDebugEnabled()){
						log.debug("theFile.exists()="+theFile.exists()+" \ntheFile.getAbsolutePath()="+theFile.getAbsolutePath());
					}
					if(theFile.exists()){
						inny=new FileInputStream(theFile);
						if(log.isDebugEnabled()){
							log.debug("theFile.getAbsolutePath()="+theFile.getAbsolutePath()+" exists");
						}
					}
				}
				File inputFile=null;
				if(null==inny){
					if(log.isDebugEnabled()){
						log.debug("in JcloudsUtility static code, propsfile trying to use propsfile path: /home/docs/Tomcat/latest/webapps/rax-autodeploy/WEB-INF/classes/props.properties");
					}
					//We are using the rax-autodeploy properties file, we may want to consider adding this properties file
					//to rax-autodeploy, but then we would have duplicat files
					inputFile=new File("/home/docs/Tomcat/latest/webapps/rax-autodeploy/WEB-INF/classes/props.properties");
					//This may be on Thu's dev environment
					if(!inputFile.exists()){
						if(log.isDebugEnabled()){
							log.debug("in JcloudsUtility static code, propsfile trying to use propsfile path: /home/docs/DeployWars/props.properties");
						}
						inputFile=new File("/home/docs/DeployWars/props.properties");
						if(!inputFile.exists()){
							if(log.isDebugEnabled()){
								log.debug("in JcloudsUtility static code, propsfile trying to use propsfile path: "+
										"/Users/thu4404/git/rax-autodeploy/src/main/resources/props.properties");
							}
							inputFile=new File("/Users/thu4404/git/rax-autodeploy/src/main/resources/props.properties");
						}
						else{
							if(log.isDebugEnabled()){
								log.debug("in JcloudsUtility static code, propsfile path=/home/docs/DeployWars/props.properties exists!!!");
							}
						}
					}
					else{
						if(log.isDebugEnabled()){
							log.debug("in JcloudsUtility static code, /home/docs/Tomcat/latest/webapps/rax-autodeploy/WEB-INF/classes/props.properties exists!!!");
						}
					}
					inny=new FileInputStream(inputFile);
				}
				else{
					if(log.isDebugEnabled()){
						log.debug("in JcloudsUtility static code, using props.properties in class path");
					}
				}
				propsFile=new Properties();
				propsFile.load(inny);
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": END:");
				}
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("IOException caught, message: "+e.getMessage());
			}
			catch(Throwable e){
				e.printStackTrace();
				log.error("IOException caught, message: "+e.getMessage());
			}		
		}
	}


	public static boolean disableMonitor(String entityId, String checkId)throws JCloudsException{
		boolean retVal=true;
		changeMonitor(entityId,checkId,false);
		return retVal;
	}

	public static boolean enableMonitor(String entityId, String checkId)throws JCloudsException{
		boolean retVal=true;
		changeMonitor(entityId,checkId,true);
		return retVal;
	}

	public static List<EntityChecks> listChecks(String entityId)throws IllegalArgumentException, JCloudsException{
		List<EntityChecks> retVal=new ArrayList<EntityChecks>();
		if(null!=entityId && !entityId.isEmpty()){
			String command="/usr/local/bin/raxmon-checks-list --details --entity-id="+entityId;
			String[] results=runCommand(command);

			System.out.println("results[0]="+results[0]+"\n");
			System.out.println("results[1]="+results[1]+"\n");
			if(!results[1].isEmpty()){
				throw new JCloudsException(results[1]);
			}
			else if(!results[0].isEmpty()){

				String[]jsonObjs=results[0].split("\\{\\'details\\'");
				System.out.println("jsonObjs.length="+jsonObjs.length);

				try {

					for(String aJsonObj:jsonObjs){
						if(!aJsonObj.isEmpty()){
							String aJsonStr="{'details' "+aJsonObj;
							System.out.println("&&&&&&&&aJsonStr="+aJsonStr);
							org.json.JSONObject anOutputJsonObj=new org.json.JSONObject(aJsonStr);	
							String checkId=anOutputJsonObj.getString("id");
							String checkLabel=anOutputJsonObj.getString("label");
							boolean disabled=anOutputJsonObj.getBoolean("disabled");
							System.out.println("checkId="+checkId+" checkLabel="+checkLabel+" disabled="+disabled);
							EntityChecks anEntityCheck=new EntityChecks(entityId,checkId,checkLabel,disabled);
							retVal.add(anEntityCheck);
						}
					}            	   
				} 
				catch (org.json.JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(Throwable e){
					e.printStackTrace();
				}
			}
		}
		else{
			if(null==entityId){
				throw new IllegalArgumentException("Entity ID can not be null");
			}
			else if(entityId.isEmpty()){
				throw new IllegalArgumentException("Entity ID can not be empty");
			}
		}
		return retVal;
	}

	public static void changeMonitor(String entityId, String checkId, boolean enable)throws IllegalArgumentException, JCloudsException{
		String METHOD_NAME="changeMonitor()";
		if(null!=entityId && !entityId.isEmpty() && null!=checkId && !checkId.isEmpty()){
			String s = null;
			String command="";

			if(enable){
				command="/usr/local/bin/raxmon-checks-enable --entity-id="+entityId+" --id="+checkId;
			}
			else{
				command="/usr/local/bin/raxmon-checks-disable --entity-id="+entityId+" --id="+checkId;;
			}
			String[]messages=runCommand(command);
			if(!messages[1].isEmpty()){
				throw new JCloudsException("Could not change monitor command="+
						command+" error message: "+messages[1]);
			}
		}
		else{
			if(null==entityId){
				throw new IllegalArgumentException("Entity ID can not be null");
			}
			else if(entityId.isEmpty()){
				throw new IllegalArgumentException("Entity ID can not be empty");
			}
			if(null==checkId){
				throw new IllegalArgumentException("Check ID can not be null");
			}
			else if(checkId.isEmpty()){
				throw new IllegalArgumentException("Check ID can not be empty");
			}
		}
	}
	//retVal[0] returns the success message while retVal[1] returns any failure message
	private static String[] runCommand(String command){
		String METHOD_NAME="changeMonitor()";
		String[] retVal=new String[2];
		retVal[0]=new String("");
		retVal[1]=new String("");
		if(null!=command && !command.isEmpty()){
			try {
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader stdInput = new BufferedReader(new 
						InputStreamReader(p.getInputStream()));

				BufferedReader stdError = new BufferedReader(new 
						InputStreamReader(p.getErrorStream()));

				// read the output from the command

				StringBuffer output=new StringBuffer("");
				String s="";
				while ((s = stdInput.readLine()) != null) {
					//System.out.println(s);
					output.append(s.trim());
				}
				retVal[0]=output.toString();

				output=new StringBuffer("");
				System.out.println("Error id any:");
				while ((s = stdError.readLine()) != null) {
					log.debug(METHOD_NAME+":"+s);
					//System.out.println(s);
					output.append(s.trim());
				}
				retVal[1]=output.toString();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retVal[1]+=" IOException caught: "+e.getMessage();
			} 
		}
		return retVal;
	}

	//example of raxmon command:
	//raxmon-alarms-create --entity-id="enrzJPacBA" --check-id="chQnRku4uf" --label="blah blah" --notification-plan-id="npTechnicalContactsEmail" --criteria="if (metric['duration'] > 25000) { return new AlarmStatus(CRITICAL, 'HTTP request took more than 25000 milliseconds.');} if (metric['duration'] > 20000) {return new AlarmStatus(WARNING, 'HTTP request took more than 20000 milliseconds.');} " 
	private static String[] createAnAlarm(String entityId, String checkId, String label, String criteria, String notificationPlan){
		String METHOD_NAME="createAnAlarm()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: entityId="+entityId+" checkId="+checkId+" label="+label+
					" notificationPlan="+notificationPlan+"criteria="+criteria);
		}
		if(null==entityId||null==checkId||null==label||null==criteria||null==notificationPlan||
				entityId.trim().isEmpty()||checkId.trim().isEmpty()||label.trim().isEmpty()||criteria.trim().isEmpty()||
				notificationPlan.trim().isEmpty()){
			return null;
		}
		String command="/usr/local/bin/raxmon-alarms-create --entity-id="+entityId+" --check-id="+checkId+" --label=\""+
				label+"\" --criteria=\""+criteria+"\" --notification-plan-id="+notificationPlan;	
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": command="+command);
		}
		String[]retVal=runCommand(command);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: ");
		}
		return retVal;				
	}

	public static List<JSONObject> getAlarms(Collection<DocToolsEntity>entities){
		List<JSONObject>retList=new ArrayList<JSONObject>();
		if(null!=entities){
			for(DocToolsEntity anEntity:entities){
				String anEntityId=anEntity.getId();
				String[] retVal=runCommand("/usr/local/bin/raxmon-alarms-list --details --entity-id="+anEntityId+"");

				if(!retVal[1].isEmpty()){
					System.out.println("There is an error: retVal[1]="+retVal[1]);
				}
				else{
					String aJsonObjStr=retVal[0];
					aJsonObjStr=removeJunk(aJsonObjStr);
					//System.out.println("~~~~aJsonObjStr="+aJsonObjStr);
					if(null!=aJsonObjStr && !aJsonObjStr.trim().isEmpty()){		    	
						//System.out.println("~~~~aJsonObjStr="+aJsonObjStr);
						JSONObject aJsonObj=null;
						try {
							String[] jsonObjectsArr=aJsonObjStr.split("\\{\\'check_id");
							if(null!=jsonObjectsArr){
								for(String aJSONObjStr:jsonObjectsArr){
									if(null!=aJSONObjStr && !aJSONObjStr.trim().isEmpty()){			    					
										String aJSONStr="{'check_id"+aJSONObjStr;
										aJsonObj = new JSONObject(aJSONStr);		    		
										retList.add(aJsonObj);
									}
								}
							}
						} 
						catch (JSONException e) {
							log.error(e);
							e.printStackTrace();
						}
						catch (Throwable e) {
							log.error(e);
							e.printStackTrace();
						}
					}
				}			
			}
		}
		return retList;
	}	

	public static Map<String, DocToolsEntity> getEntities()throws JCloudsException{
		String METHOD_NAME="getEntities()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		Map<String, DocToolsEntity> retVal=new LinkedHashMap<String,DocToolsEntity>();

		String[]retStrings=runCommand("/usr/local/bin/raxmon-entities-list --details");

		if(!retStrings[1].isEmpty()){
			throw new JCloudsException(retStrings[1]);
		}
		else{
			String outputStr=retStrings[0];

			outputStr=outputStr.replaceAll("\\(u'", "('");
			outputStr=outputStr.replaceAll(": u'", ": '");
			outputStr=outputStr.replaceAll(", u'", ", '");
			outputStr=outputStr.replaceAll(",u'", ",'");
			int index=outputStr.lastIndexOf("}");
			if((index+1)<(outputStr.length()-1));
			outputStr=outputStr.substring(0,(index+1));

			//               {'agent_id': None,
			//            	   'driver': <rackspace_monitoring.drivers.rackspace.RackspaceMonitoringDriver object at 0x106c13c50>,
			//            	   'extra': {},
			//            	   'id': 'en8zdJiN3l',
			//            	   'ip_addresses': [('public0_v4', '166.78.17.115'),
			//            	                    ('access_ip0_v4', '166.78.17.115'),
			//            	                    ('access_ip1_v6',
			//            	                     '2001:4800:7811:0513:41bb:c43b:ff04:7f6a'),
			//            	                    ('public1_v6', '2001:4800:7811:0513:41bb:c43b:ff04:7f6a'),
			//            	                    ('public0_v4', '10.181.130.193')],
			//            	   'label': 'zonapellucida2',
			//            	   'uri': 'https://dfw.servers.api.rackspacecloud.com/694977/servers/7a127933-8c09-4c73-90fe-2d362670a6ea'}
			//               
			//               ...
			//               
			String[]jsonObjs=outputStr.split("\\{\\'agent_id\\'");

			try {

				for(String aJsonObj:jsonObjs){
					if(!aJsonObj.isEmpty()){
						String aJsonStr="{'agent_id' "+aJsonObj;
						org.json.JSONObject anOutputJsonObj=new org.json.JSONObject(aJsonStr);
						org.json.JSONArray ipAddressesJsonArray=anOutputJsonObj.getJSONArray("ip_addresses");
						org.json.JSONArray publicIpAddJsonArr=ipAddressesJsonArray.getJSONArray(1);
						String publicJsonIPAddrKey=publicIpAddJsonArr.getString(0);
						String publicJsonIPAddrVal=publicIpAddJsonArr.getString(1);
						if(null==publicJsonIPAddrKey || !publicJsonIPAddrKey.equalsIgnoreCase(DeployUtility.PRIVATE_JSON_IP_ADDRESS)){
							for(int i=1;i<ipAddressesJsonArray.length();++i){
								publicIpAddJsonArr=ipAddressesJsonArray.getJSONArray(i);
								String aKey=publicIpAddJsonArr.getString(0);
								if(null!=aKey && aKey.equalsIgnoreCase(DeployUtility.PRIVATE_JSON_IP_ADDRESS)){
									publicJsonIPAddrKey=aKey;
									publicJsonIPAddrVal=publicIpAddJsonArr.getString(1);
									break;
								}
							}
						}
						DocToolsEntity anEntity=new DocToolsEntity();
						anEntity.setPrjsonivate0_v4(publicJsonIPAddrVal);
						anEntity.setPublicIP(ipAddressesJsonArray.getJSONArray(0).getString(1));
						anEntity.setId(anOutputJsonObj.getString("id"));
						anEntity.setLabel(anOutputJsonObj.getString("label"));
						anEntity.setUri(anOutputJsonObj.getString("uri"));

						retVal.put(anEntity.getPrjsonivate0_v4(),anEntity);
					}
				}            	   
			} 
			catch (org.json.JSONException e) {
				log.error(e);
				e.printStackTrace();
			}
			catch (Throwable e) {
				log.error(e);
				e.printStackTrace();
			}
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": END:");
			}
			return retVal;
		}
	}

	public static Map<String,Node> getEnabledLoadBalancerNodes(LoadBalancer loadBalancer, CloudLoadBalancersApi clb, HttpServletRequest request){
		String METHOD_NAME="getEnabledLoadBalancerNodes()";
		JCloudsUtility.loadPropsFile(request);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		Map<String,Node>retVal=new LinkedHashMap<String,Node>();

		Map<Integer,Node>nodes=getLoadBalancerNodes(loadBalancer, clb);
		ServerApi serverApi=JCloudsUtility.getServerApi(request,"IAD");

		for(Node aNode:nodes.values()){
			Condition con=aNode.getCondition();
			if(con==Condition.ENABLED){
				retVal.put(aNode.getAddress(),aNode);
				if(log.isDebugEnabled()){
					String aKey=aNode.getAddress();
					int id=aNode.getId();
					
					log.debug(METHOD_NAME+": key="+aKey+" added aNode.getId()="+id);

					Server server=serverApi.get(id+"");
					if(null!=server){
						String publiAddress=server.getAccessIPv4();
						log.debug(METHOD_NAME+"*****publiAddress="+publiAddress);
					}
					else{
						log.debug(METHOD_NAME+": server is NULL!!!");
					}
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal.size()="+retVal.size());
		}
		return retVal;
	}

	public static Map<Integer,Node> getLoadBalancerNodes(LoadBalancer loadBalancer, CloudLoadBalancersApi clb){
		Map<Integer,Node>retVal=new LinkedHashMap<Integer,Node>();
		NodeApi nodeApi=clb.getNodeApiForZoneAndLoadBalancer("DFW", loadBalancer.getId());

		for(Node aNode:nodeApi.list().concat()){
			retVal.put(aNode.getId(),aNode);		
		}
		return retVal;
	}
	
	private static ServerApi getServerApi(HttpServletRequest request, String zone){
		String METHOD_NAME="getServerApi()";
		ServerApi retVal=null;
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: zone="+zone);
		}
		if(null!=zone && !zone.isEmpty()){
			JCloudsUtility.loadPropsFile(request);

			String provider = propsFile.getProperty("jcloudsserversprovider","rackspace-cloudloadbalancers-us");
			String username = propsFile.getProperty("jcloudsusername","mossoths");
			String apiKey = propsFile.getProperty("apikey","99b917af206ae042f3291264e0b78a84");

			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": provider="+provider);
				log.debug(METHOD_NAME+": username="+username);
				log.debug(METHOD_NAME+": apiKey="+apiKey);
			}		
			NovaApi novaApi=ContextBuilder.newBuilder(provider)//ComputeServiceContext context=ContextBuilder.newBuilder("rackspace-cloudservers-us")
					.credentials(username, apiKey)
					.buildApi(NovaApi.class);
			retVal=novaApi.getServerApiForZone(zone);	
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
		return retVal;
	}

	public static CloudLoadBalancersApi getCloudLoadBalancer(HttpServletRequest request){
		String METHOD_NAME="getCloudLoadBalancer()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":START:");
		}
		JCloudsUtility.loadPropsFile(request);
		//String username="mossoths";
		//String apiKey="99b917af206ae042f3291264e0b78a84";
		//String username="thudoan";
		//String apiKey="89a73d927249a5a8a803cc368a81ccb1";
		String provider = propsFile.getProperty("jcloudsprovider","rackspace-cloudloadbalancers-us");
		String username = propsFile.getProperty("jcloudsusername","mossoths");
		String apiKey = propsFile.getProperty("apikey","99b917af206ae042f3291264e0b78a84");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+":provider="+provider);
			log.debug(METHOD_NAME+":username="+username);
			log.debug(METHOD_NAME+":apiKey="+apiKey);
			log.debug(METHOD_NAME+":END:");
		}		
		return ContextBuilder.newBuilder(provider)
				.credentials(username, apiKey)				
				.buildApi(CloudLoadBalancersApi.class);

	}

	public static void waitForActive(CloudLoadBalancersApi clb, 
			LoadBalancer loadBalancer) throws TimeoutException {
	      // Wait for the Load Balancer to become Active before moving on
	      // If you want to know what's happening during the polling, enable logging. See
	      // /jclouds-example/rackspace/src/main/java/org/jclouds/examples/rackspace/Logging.java
	      if (!LoadBalancerPredicates.awaitAvailable(clb.getLoadBalancerApiForZone(ZONE)).apply(loadBalancer)) {
	         throw new TimeoutException("Timeout on loadBalancer: " + loadBalancer);
	      }
	}	

	public static LoadBalancer getLoadBalancer(HttpServletRequest request, CloudLoadBalancersApi clb){
		String METHOD_NAME="getLoadBalancer()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START:");
		}
		JCloudsUtility.loadPropsFile(request);
		String loadBalancerName=propsFile.getProperty("loadbalancer","docs.rackspace.com-lb");
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": !@!@!@!@!@!@!loadBalancerName="+loadBalancerName);
		}
		for(LoadBalancer loadBalancer: clb.getLoadBalancerApiForZone(ZONE).list().concat()){

			if(loadBalancer.getName().startsWith(loadBalancerName)){
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": END:");
				}
				return loadBalancer;
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": Error Throwing RuntimeException:");
		}
		throw new RuntimeException(loadBalancerName+" not found. Run a CreateBalancer* example first.");		   
	}

	//These create methods are not currently used, but is implemented in case we need to create Alarms in the future
	private static void createConnectionTimeAlarm(List<JSONObject>alarmsJSONObj, String entityId, String checkId)throws JSONException{
		createGenericAlarm(alarmsJSONObj, entityId, checkId, true);
	}

	private static void createStatusCodeAlarm(List<JSONObject>alarmsJSONObj, String entityId, String checkId)throws JSONException{
		createGenericAlarm(alarmsJSONObj, entityId, checkId, false);
	}

	private static void createGenericAlarm(List<JSONObject>alarmsJSONObj, String entityId, String checkId, boolean isConnection)throws JSONException{
		String METHOD_NAME="createGenericAlarm()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: entityId="+entityId+" checkId="+checkId+" isConnection="+isConnection);
		}
		boolean createAlarm=true;
		for(JSONObject anAlarmJSONObj:alarmsJSONObj){
			String aLabel=anAlarmJSONObj.getString("label");
			if(null!=aLabel){
				if(isConnection){
					if(aLabel.equals(CONNECTION_TIME_LABEL)){
						createAlarm=false;
					}
				}
				else{
					if(aLabel.equals(STATUS_CODE_LABEL)){
						createAlarm=false;
					}
				}
			}
		}		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": createAlarm="+createAlarm+" isConnection="+isConnection);
		}
		if(createAlarm){
			if(isConnection){
				createAnAlarm(entityId, checkId, CONNECTION_TIME_LABEL, CONNECTION_TIME_ALARM_CRITERIA, NOTIFICATION_PLAN);
			}
			else{
				createAnAlarm(entityId, checkId, STATUS_CODE_LABEL, STATUS_CODE_ALARM_CRITERIA, NOTIFICATION_PLAN);
			}
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: ");
		}		
	}	

	//This disables a node and all associated check monitors
	public static Node disableAnEnabledNode(CloudLoadBalancersApi clb,
			LoadBalancer loadBalancer, Map<String, DocToolsEntity>entities,
			Map<String,Node>enabledNodes)throws JCloudsException{		
		String METHOD_NAME="disableAnEnabledNode()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: enabledNodes.values().size()="+enabledNodes.values().size());
		}
		Node retVal=null;
		Node aNode=null;
		Iterator<Node>iter=null;
		DocToolsEntity anEntity=null;

		for(iter=enabledNodes.values().iterator();iter.hasNext();){
			aNode=iter.next();
			if(log.isDebugEnabled()){
				log.debug(METHOD_NAME+": aNode="+aNode);
			}
			try {	

				anEntity=DeployUtility.disableEntityChecks(entities,aNode);
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": disabled checks for entity:"+anEntity);
				}				
				disableNode(clb, loadBalancer, aNode.getId());
				if(log.isDebugEnabled()){
					log.debug(METHOD_NAME+": aNode.getId()="+aNode.getId()+" is disabled");
				}
				retVal=aNode;
				break;
			} 
			catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retVal=null;
				if(iter.hasNext()){
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": Timeout error, there is another available node, try to disable that one");
					}
					try{
						enableNode(clb, loadBalancer, aNode.getId());
						//Now try to enable monitors and nodes that may have been disabled
						DeployUtility.enableEntityChecks(entities, aNode);
					} 
					catch (TimeoutException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					aNode=iter.next();
					retVal=aNode;
					try{
						anEntity=DeployUtility.disableEntityChecks(entities,aNode);
						disableNode(clb, loadBalancer, aNode.getId());
						retVal=aNode;
						break;
					}
					catch(TimeoutException e2){
						e2.printStackTrace();
						throw new JCloudsException("Timeout error, second try to disable node failed, InsalledWarsServlet."+
								METHOD_NAME+": message:"+e.getMessage());
					}
				}
				else{
					if(log.isDebugEnabled()){
						log.debug(METHOD_NAME+": Timeout error, there is no other available node");
					}
					retVal=null;
					throw new JCloudsException("TimeoutException, no additional nodes to try, InstalledWarsServlet."+
							METHOD_NAME+": message:"+e.getMessage());					
				}
			}
			catch(Throwable e){
				e.printStackTrace();
			}
		}
		if(null==anEntity){
			throw new JCloudsException("No matching DocToolsEntity and Node could be found anEntity is null");
		}
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END: retVal="+retVal);
		}
		return retVal;
	}

	public static void disableNode(CloudLoadBalancersApi clb, 
			LoadBalancer loadBalancer, int node) throws TimeoutException{
		String METHOD_NAME="disableNode()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: Trying to disable node: "+node);
		}
		waitForActive(clb,loadBalancer);
		NodeApi nodeApi=clb.getNodeApiForZoneAndLoadBalancer(ZONE, loadBalancer.getId());
		
		UpdateNode updateNode=UpdateNode.builder()
				.condition(Condition.DISABLED)
				.build();

		nodeApi.update(node, updateNode);
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}

	public static void enableNode(CloudLoadBalancersApi clb, 
			LoadBalancer loadBalancer, int node) throws TimeoutException{
		String METHOD_NAME="enableNode()";
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": START: Trying to enableNode node="+node);
		}
		waitForActive(clb,loadBalancer);
		NodeApi nodeApi=clb.getNodeApiForZoneAndLoadBalancer(ZONE, loadBalancer.getId());
		
		UpdateNode updateNode=UpdateNode.builder()
				.condition(Condition.ENABLED)
				.build();
		nodeApi.update(node, updateNode);
		
		if(log.isDebugEnabled()){
			log.debug(METHOD_NAME+": END:");
		}
	}

	private static String removeJunk(String theStr){
		theStr=theStr.replaceAll("\\(u'", "('");
		theStr=theStr.replaceAll(": u'", ": '");
		theStr=theStr.replaceAll(", u'", ", '");
		theStr=theStr.replaceAll(",u'", ",'");
		theStr=theStr.replaceAll("criteria': u","criteria': ");
		int index=theStr.lastIndexOf("}");
		if((index+1)<(theStr.length()-1)){
			theStr=theStr.substring(0,(index+1));	
		}
		return theStr;
	}	


}
