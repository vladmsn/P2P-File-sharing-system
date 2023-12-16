import org._ubb.model.KBucket;
import org._ubb.model.KademliaNode;
import org._ubb.utils.EncryptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {

    private KademliaNode kademliaNode;

    @BeforeEach
    public void setUp() {
        kademliaNode = new KademliaNode("127.0.0.1", 8080);
    }

    @Test
    public void testAddNodeToRoutingTable() {
        String testNodeId = EncryptionUtils.generateNodeIdentifier("127.0.0.1", 8081);
        String connectionDetails = "127.0.0.1:8081";

        kademliaNode.addNodeToRoutingTable(testNodeId, connectionDetails);
        // Verify that the node was added to the correct k-bucket
        int bucketIndex = kademliaNode.getBucketIndex(testNodeId);
        KBucket bucket = kademliaNode.getRoutingTable().get(bucketIndex);
        assertTrue(bucket.containsNode(testNodeId));
    }

    @Test
    public void testRemoveNodeFromRoutingTable() {
        String testNodeId = EncryptionUtils.generateNodeIdentifier("127.0.0.1", 8081);
        String connectionDetails = "127.0.0.1:8081";

        kademliaNode.addNodeToRoutingTable(testNodeId, connectionDetails);
        kademliaNode.removeNodeFromRoutingTable(testNodeId);
        // Verify that the node was removed from the k-bucket
        int bucketIndex = kademliaNode.getBucketIndex(testNodeId);
        KBucket bucket = kademliaNode.getRoutingTable().get(bucketIndex);
        assertFalse(bucket.containsNode(testNodeId));
    }

    @Test
    public void testDataStoreOperations() {
        String fileId = "file123";
        String filePath = "/path/to/file123";

        kademliaNode.addToDataStore(fileId, filePath);
        assertEquals(filePath, kademliaNode.getFromFileStore(fileId));

        kademliaNode.removeFromDataStore(fileId);
        assertNull(kademliaNode.getFromFileStore(fileId));
    }


    @Test
    public void testDifferentBucketIndices() {
        // Assuming the node's own identifier is a known value
        String ownNodeId = kademliaNode.getIdentifier(); // Add a getter for identifier in Node class if needed

        // Manually craft node IDs that are known to differ at specific bits
        String nodeId1 = modifyBit(ownNodeId, 159); // Differ at the highest bit
        String nodeId2 = modifyBit(ownNodeId, 0);   // Differ at the lowest bit

        int bucketIndex1 = kademliaNode.getBucketIndex(nodeId1);
        int bucketIndex2 = kademliaNode.getBucketIndex(nodeId2);

        assertNotEquals(bucketIndex1, bucketIndex2);
    }

    private String modifyBit(String nodeId, int bitPosition) {
        BigInteger id = new BigInteger(nodeId, 16);
        BigInteger modifiedId = id.flipBit(bitPosition); // Flip the specified bit
        return modifiedId.toString(16);
    }
}
