import java.io.Serializable;

public class token implements Serializable{
    Integer tokenId=0;
    String srcNodeId;
    Integer nodePathSum=0;
    Integer nodeSum=0;
    String startingNode;
   	public token(String nodeId, Integer NodeLabel,String startNode,Integer tokenId){
   		if (startNode == null)
   			startingNode = nodeId;
   		else
   			startingNode = startNode;
    	srcNodeId = nodeId;
    	nodeSum = NodeLabel;
	this.tokenId = tokenId;
    }
}
