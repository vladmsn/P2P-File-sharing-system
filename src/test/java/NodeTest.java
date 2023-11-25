import org._ubb.model.KBucket;
import org._ubb.model.Node;
import org._ubb.utils.EncryptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {

    private Node node;

    @BeforeEach
    public void setUp() {
        node = new Node("127.0.0.1", 8080);
    }

    @Test
    public void testAddNodeToRoutingTable() {
        String testNodeId = EncryptionUtils.generateNodeIdentifier("127.0.0.1", 8081);
        String connectionDetails = "127.0.0.1:8081";

        node.addNodeToRoutingTable(testNodeId, connectionDetails);
        // Verify that the node was added to the correct k-bucket
        int bucketIndex = node.getBucketIndex(testNodeId);
        KBucket bucket = node.getRoutingTable().get(bucketIndex);
        assertTrue(bucket.containsNode(testNodeId));
    }

    @Test
    public void testRemoveNodeFromRoutingTable() {
        String testNodeId = EncryptionUtils.generateNodeIdentifier("127.0.0.1", 8081);
        String connectionDetails = "127.0.0.1:8081";

        node.addNodeToRoutingTable(testNodeId, connectionDetails);
        node.removeNodeFromRoutingTable(testNodeId);
        // Verify that the node was removed from the k-bucket
        int bucketIndex = node.getBucketIndex(testNodeId);
        KBucket bucket = node.getRoutingTable().get(bucketIndex);
        assertFalse(bucket.containsNode(testNodeId));
    }

    @Test
    public void testDataStoreOperations() {
        String fileId = "file123";
        String filePath = "/path/to/file123";

        node.addToDataStore(fileId, filePath);
        assertEquals(filePath, node.getFromFileStore(fileId));

        node.removeFromDataStore(fileId);
        assertNull(node.getFromFileStore(fileId));
    }


    @Test
    public void testDifferentBucketIndices() {
        // Assuming the node's own identifier is a known value
        String ownNodeId = node.getIdentifier(); // Add a getter for identifier in Node class if needed

        // Manually craft node IDs that are known to differ at specific bits
        String nodeId1 = modifyBit(ownNodeId, 159); // Differ at the highest bit
        String nodeId2 = modifyBit(ownNodeId, 0);   // Differ at the lowest bit

        int bucketIndex1 = node.getBucketIndex(nodeId1);
        int bucketIndex2 = node.getBucketIndex(nodeId2);

        assertNotEquals(bucketIndex1, bucketIndex2);
    }

    private String modifyBit(String nodeId, int bitPosition) {
        BigInteger id = new BigInteger(nodeId, 16);
        BigInteger modifiedId = id.flipBit(bitPosition); // Flip the specified bit
        return modifiedId.toString(16);
    }
}
