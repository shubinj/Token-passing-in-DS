import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Semaphore;
import java.util.Set;

@SuppressWarnings("unused")
public class Project1 {
    Integer tokenSent = 0;
    Integer tokenReceived = 0;
    String hashName;
    String nodeId=null;
    String nextNodeId;
    String username = "sxj148030";
    List<Integer> nodeList=new ArrayList<Integer>();    
    Hashtable<String,String> nodeHash = new Hashtable<String,String>();
    Hashtable<String,String> nodePath = new Hashtable<String,String>();
	Random Label = new Random();	
	File file = null;
	BufferedWriter output = null;
	Integer nodeLabel = Label.nextInt(651);
    
   public Project1(String nodeId, String fileName) throws IOException{ 
   			File fName = new File(fileName);
			FileReader configFile = new FileReader(fileName);
			BufferedReader bRead = new BufferedReader(configFile);
			String newLine = null;
			int grpHash=1;
			this.nodeId=nodeId;
			
			String tmpF = fName.getName();
			String tmpN = tmpF.split("\\.(?=[^\\.]+$)")[0];
			this.file = new File(tmpN+"-"+ username +"-"+nodeId+".out");
			this.output = new BufferedWriter(new FileWriter(this.file));
			
			while ((newLine = bRead.readLine()) != null)
			{  
				if(newLine.isEmpty())
					continue;
				if (newLine.startsWith("#"))
					continue;
				String[] tmpList = newLine.split("\\s+");
				if(nodeHash.containsKey(tmpList[0])){
				       grpHash = 2;
				}

				if((grpHash == 1) && (tmpList.length!=3))
					continue;

				if (grpHash == 1){
					String[] nodeList = newLine.split("\\s+");
					nodeHash.put(nodeList[0],nodeList[1]+":"+nodeList[2]);					
				}				
				if (grpHash == 2){					
					newLine = newLine.split("#")[0];
					String[] nodeList = newLine.split("\\s+");
					int i;
					String tmpLine=null;
					for(i=1;i< nodeList.length;i++)
					{
						if (nodeList[i].startsWith("#"))
							continue;
						if (tmpLine == null)
							tmpLine = nodeList[i];
						else
							tmpLine = tmpLine +":"+ nodeList[i];
					}
					
					//return node to the initiating process.
					String tmpName = nodeList[0]+"_";
					while(nodePath.containsKey(tmpName)){
						tmpName =  tmpName+"+";
					}
				    if (tmpLine == null)
				    	tmpLine = nodeList[0];
				    else{
				    	tmpLine=tmpLine +":"+ nodeList[0];
					}
				    nodePath.put(tmpName,tmpLine);									
				}		

			}
   }

	public void sendToken(token objToken,String nextNodeId) throws IOException
			{
			    String message;
			    //Create a client socket and connect to server at 127.0.0.1 port 5000
			    String[] tmpNode = this.nodeHash.get(nextNodeId).split(":"); 
	          	    Socket clientSocket = new Socket(tmpNode[0],Integer.valueOf(tmpNode[1]));
			    OutputStream os = clientSocket.getOutputStream();
			    ObjectOutputStream oos = new ObjectOutputStream(os);
			    oos.writeObject(objToken);
			    oos.flush();
			    oos.reset();
		
			    oos.close();
			    os.close();
			    clientSocket.close();
		}
    
	
	public String generateNextNodeId(token tmpToken) throws IOException{
        String[] nodePathList = this.nodePath.get(tmpToken.startingNode).split(":");
        String nextNodeId ="null";
	try{
        if (tmpToken.nodePathSum < nodePathList.length ){
        	nextNodeId = nodePathList[tmpToken.nodePathSum];  	
		}
	return nextNodeId;
	}
	catch(Exception e){
	       System.out.println("Exception occured in generateNextNodeId in Node:"+this.nodeId+" for token:"+tmpToken.srcNodeId+" at path:"+Integer.toString(tmpToken.nodePathSum));
	       return "exception occured!";
	   
	  }}


	   public void runTest1(token rcvToken) throws IOException,ClassNotFoundException{
	   	   String nextNode = generateNextNodeId(rcvToken);
		   output = new BufferedWriter(new FileWriter(this.file,true));
		   Integer tokenSent = this.tokenSent;
		   Integer tokenReceived = this.tokenReceived;
		   class processToken implements Runnable{
	    		public processToken(token rcvToken,BufferedWriter output) throws IOException{
	    			    		
	    	   	   	    if (!nextNode.matches("null")){                        	 
	    	   	          	rcvToken.nodeSum = rcvToken.nodeSum+ Integer.valueOf(nodeLabel);
	    	   		        rcvToken.nodePathSum++;
	    	   	         	sendToken(rcvToken,nextNode);
	    	   	         }                        	
	    	   			else{
					 	output.write("\nReceived token "+Integer.toString(rcvToken.tokenId)+"\tToken sum = "+Integer.toString(rcvToken.nodeSum));
						if (tokenReceived == tokenSent){
	    	   					output.write("\nAll tokens received");						
						}
	    	   			}
	    				output.flush();
	    				output.close();
	    	   	        //Thread.yield();
	    		}

				@Override
				public void run() {
					// TODO Auto-generated method stub
					
				}
	    	}
		processToken p = new processToken(rcvToken,output);
	    	Thread runThread = new Thread(p);
	    	runThread.start();
	    		    
	    }
	   
    public void socketListen() throws ClassNotFoundException,InterruptedException,IOException{
            //Create a server socket at port 5000
            	ServerSocket serverSock = null;
		String[] nodeList = this.nodeHash.get(this.nodeId).split(":");     			
     		serverSock = new ServerSocket(Integer.valueOf(nodeList[1]),this.nodeHash.size());

        	//Server goes into a permanent loop accepting connections from clients
	        while(true)
            {
	            //Listens for a connection to be made to this socket and accepts it
               			 Socket sock;
				sock = serverSock.accept();			
				token rcvToken = null;
				ObjectInputStream os  = new ObjectInputStream(sock.getInputStream());
				rcvToken=(token)os.readObject();
				String nextNode = generateNextNodeId(rcvToken);
				if (nextNode.matches("null") && rcvToken.srcNodeId.equals(this.nodeId))
				       this.tokenReceived = this.tokenReceived + 1;

				runTest1(rcvToken);				
                 		Thread.yield();				
            }
            
    }
    
 
 
    public void runTest()
	{		
		// Inject 1000 tasks into the executor in a separate thread. 
		Runnable inserter = new Runnable() {
			public void run()
			{
				try {
					socketListen();
					//Thread.yield();
				}
				catch (ClassNotFoundException e) {
				     // TODO Auto-generated catch block
                     e.printStackTrace();
                }
				catch (InterruptedException e){
				     // TODO Auto-generated catch block
                    e.printStackTrace();
               } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		};
		Thread insertThread = new Thread(inserter);
		insertThread.start();
	}

public static void main(String args[]) throws IOException,InterruptedException
{
    Integer i=1;
    final Project1 SampleServerObj = new Project1(args[0],args[1]);
    SampleServerObj.output.write("Net ID: "+SampleServerObj.username);
	SampleServerObj.output.write("\nNode ID: "+SampleServerObj.nodeId);
	SampleServerObj.output.write("\nListening on "+SampleServerObj.nodeHash.get(SampleServerObj.nodeId));
	SampleServerObj.output.write("\nRandom number: "+SampleServerObj.nodeLabel);
	SampleServerObj.output.close();
  	SampleServerObj.runTest();
	Set<String> en = SampleServerObj.nodePath.keySet();
	   for(String tmp: en){
	          if(tmp.contains(SampleServerObj.nodeId))
		  	SampleServerObj.tokenSent = SampleServerObj.tokenSent + 1;
		}
   	   if ((SampleServerObj.tokenSent == SampleServerObj.tokenReceived) && (SampleServerObj.tokenSent==0)){
           	   SampleServerObj.output = new BufferedWriter(new FileWriter(SampleServerObj.file,true));
		   SampleServerObj.output.write("\nAll tokens received");
		   SampleServerObj.output.close();
		   }

	  Thread.sleep(4000);
	  for(String tmp: en){
	  	   Thread.sleep(1000);
		   if(tmp.contains(SampleServerObj.nodeId+"_")){	 
			   token myToken = new token(SampleServerObj.nodeId, SampleServerObj.nodeLabel,tmp,i);
			   i=i+1;
			   String nextNode = SampleServerObj.generateNextNodeId(myToken);
			   if (nextNode.equalsIgnoreCase("null"))
				   nextNode = SampleServerObj.nodeId;			   
			   myToken.nodePathSum++;
			   SampleServerObj.output = new BufferedWriter(new FileWriter(SampleServerObj.file,true));
			   SampleServerObj.output.write("\nEmitting token "+Integer.toString(myToken.tokenId)+" with path "+SampleServerObj.nodeId+" -> "+SampleServerObj.nodePath.get(myToken.startingNode).replace(":"," -> " ));
			   SampleServerObj.sendToken(myToken,nextNode);
			   SampleServerObj.output.close();
		   }
		   
	  }
}
}

