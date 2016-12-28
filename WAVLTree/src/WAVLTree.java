/**
 *  Made By:
 *  tamirdennis - 208538702
 *  AND
 *  Fill here
 *
 *  WAVLTree
 *
 * An implementation of a WAVL Tree with distinct integer keys and info
 */

public class WAVLTree {
    private WAVLNode root;
    private int size;
    private WAVLNode min;
    private WAVLNode max;
    private final WAVLNode external = new WAVLNode();

    /**
     * public boolean empty()
     *
     * returns true if and only if the tree is empty
     */
    public boolean empty() {
        return size == 0;
    }

    /**
     * public String search(int k)
     *
     * returns the info of an item with key k if it exists in the tree
     * otherwise, returns null
     */
    public String search(int key) {
        WAVLNode nearestNode = findNearestNode(key);
        if (nearestNode == null || nearestNode.key != key) {
            return null;
        }

        return nearestNode.info;
    }

    /**
     * public int insert(int k, String i)
     *
     * inserts an item with key k and info i to the WAVL tree. the tree must
     * remain valid (keep its invariants). returns the number of rebalancing
     * operations, or 0 if no rebalancing operations were necessary. returns -1
     * if an item with key k already exists in the tree.
     */
    public int insert(int key, String info) {
        if (empty()) {
            root = new WAVLNode(key, info, null);
            min = root;
            max = root;
            size = 1;
            return 0;
        }

        //find the parent node of the new node
        WAVLNode parentNode = findNearestNode(key);
        if (parentNode.key == key) {
            return -1;
        }

        int rebalanceCount = 0;
        WAVLNode newNode = new WAVLNode(key, info, parentNode);
        boolean isParentALeaf = parentNode.isLeafNode();

        newNode.parent = parentNode;
        if (parentNode.key > key) {
            parentNode.left = newNode;
        } else {
            parentNode.right = newNode;
        }

        //update max and min if necessary
        if (newNode.key < min.key)
            min = newNode;
        if (newNode.key > max.key)
            max = newNode;

        //if parent was a leaf than it needs to be promoted, otherwise tree is  already balanced
        if (isParentALeaf) {
            parentNode.promote();
            rebalanceCount++;
            rebalanceCount = rebalanceInsert(parentNode, rebalanceCount);
        }

        size++;
        return rebalanceCount;
    }

    /**
     * Rebalance the tree after a promote occured
     * returns the number of rebalancing operations that was necessary
     */
    private int rebalanceInsert(WAVLNode node, int rebalanceCount) {
        if (node.rankDifference() == -1 || node.rankDifference() == 1)
            return rebalanceCount;
        else {
            WAVLNode parent = node.parent;
            //here node.rankDifference() = 0 and parent is not null
            if (parent.leftChildRankDifference() == 1 || parent.rightChildRankDifference() == 1) {
                //rebalance a node of the form (1,0) or (0,1) of rank differences
                parent.promote();
                rebalanceCount++;
                rebalanceCount = rebalanceInsert(parent, rebalanceCount);
            } else if (node.isARightChild()) {
                // selecting between rotating left or double rotating as rotating right and then left
                if (node.leftChildRankDifference() == 2) {
                    parent.demote();
                    rotateLeft(parent);
                    rebalanceCount += 2;
                } else {
                    //double rotation then fix ranks of rotated nodes
                    WAVLNode b = node.left;
                    rotateRightThenLeft(parent);
                    node.demote();
                    parent.demote();
                    b.promote();
                    rebalanceCount += 5;
                }
            } else {
                // selecting between rotating right or double rotating as rotating left and then right
                if (node.rightChildRankDifference() == 2) {
                    parent.demote();
                    rotateRight(parent);
                    rebalanceCount += 2;
                } else {
                    //double rotation then fix ranks of rotated nodes
                    WAVLNode b = node.right;
                    rotateLeftThenRight(parent);
                    node.demote();
                    parent.demote();
                    b.promote();
                    rebalanceCount += 5;
                }
            }

            return rebalanceCount;
        }
    }

    /**
     * public int delete(int k)
     *
     * deletes an item with key k from the binary tree, if it is there; the tree
     * must remain valid (keep its invariants). returns the number of
     * rebalancing operations, or 0 if no rebalancing operations were needed.
     * returns -1 if an item with key k was not found in the tree.
     */
    public int delete(int key) {
        WAVLNode deletedNode = findNearestNode(key);
        if (deletedNode == null || deletedNode.key != key) {
            return -1;
        }

        size--;
        if (empty()) {
            root = null;
            min = null;
            max = null;
            return 0;
        }

        if (deletedNode.key == min.key)
            min = min.parent;
        if (deletedNode.key == max.key)
            max = max.parent;
        WAVLNode parent; // the parent of the node we deleted - used when we rebalance the tree.
        if (!(deletedNode.isUnaryNode() || deletedNode.isLeafNode())) { // choosing its successor node to replace it if its not leaf and not unary node.
            WAVLNode s = findSuccessor(deletedNode);
            parent = s.parent;
            removeNodeFromTree(s); //remove the successor node from the tree

            deletedNode.key = s.key;
            deletedNode.info = s.info;
            if (max == s) {
                max = deletedNode;
            }
            deletedNode = s;
        } else {
            parent = deletedNode.parent;
            if (deletedNode == root) { // size > 0 because we checked this option in the start of delete.
                // here is the option when we delete a root that is unary node in the tree.
                if (deletedNode.right.isExternalNode()) {
                    root = root.left;
                    root.parent = null;
                } else {
                    root = root.right;
                    root.parent = null;
                }
                deletedNode.deleteFields();
                return 0;
            } else
                removeNodeFromTree(deletedNode); //if the node is already a leaf or unary node we remove it from the tree.
        }

        deletedNode.deleteFields(); //delete the node entirely

        // rebalance the tree from now with the pointer to parent.
        int rebalanceCount = 0;
        if (parent.isLeafNode() && parent.rightChildRankDifference() == 2 && parent.leftChildRankDifference() == 2) {
            parent.demote();
            rebalanceCount++;
            parent = parent.parent;
        }

        return rebalanceDelete(parent, rebalanceCount);
    }

    /**
     * this function is fixing a violation of the WAVLTree from a specific node
     * up and returns the number of rebalancing opperations done in order to fix it.
     *
     * @param node - a node that has a rank problem with one of its children that causing a violation of the WAVLTree.
     * @param rebalanceCount number of rebalancing opperations done untill this point from the bottom of the tree.
     * @return total amount of rebalancing opperations done untill the tree is WAVLTree (from bottom to root).
     */
    private int rebalanceDelete(WAVLNode node, int rebalanceCount) {
        if ((node == null || !(node.leftChildRankDifference() == 3 || node.rightChildRankDifference() == 3))) {
            return rebalanceCount; // rebalance complete.
        }

        if (node.leftChildRankDifference() == 2 || node.rightChildRankDifference() == 2) {
            // the case of (3,2) or (2,3), in this case we demote and move up the tree
            node.demote();
            rebalanceCount++;
            return rebalanceDelete(node.parent, rebalanceCount);
        }

        if (node.leftChildRankDifference() == 3) {
            WAVLNode right = node.right;
            if (right.leftChildRankDifference() == 2 && right.rightChildRankDifference() == 2) {
                // double demote, we demote the node and it's right son and move up the tree
                node.demote();
                right.demote();
                rebalanceCount += 2;
                return rebalanceDelete(node.parent, rebalanceCount);
            }
            if (right.rightChildRankDifference() == 1) {
                //rotate - we rotate left the node with it's right son and fix ranks, and we're finished with rebalancing
                rotateLeft(node);
                right.promote();
                node.demote();
                rebalanceCount += 3;
                if (node.isLeafNode() && node.rightChildRankDifference() == 2 && node.leftChildRankDifference() == 2) {
                    node.demote();
                    rebalanceCount++;
                }

                return rebalanceCount;
            } else {
                // we double rotate and fix ranks, and we're finished with rebalancing
                WAVLNode a = right.left;
                rotateRightThenLeft(node);
                a.rank += 2;
                right.demote();
                node.rank -= 2;
                rebalanceCount += 7;
                return rebalanceCount;
            }
        } else { //- (1,3) = mirror cases of (3,1) - double demote and go up the tree
            WAVLNode left = node.left;
            if (left.leftChildRankDifference() == 2 && left.rightChildRankDifference() == 2) { //double demote
                node.demote();
                left.demote();
                rebalanceCount += 2;
                return rebalanceDelete(node.parent, rebalanceCount);
            }
            if (left.leftChildRankDifference() == 1) { //rotate and fix ranks
                rotateRight(node);
                left.promote();
                node.demote();
                rebalanceCount += 3;
                if (node.isLeafNode() && node.rightChildRankDifference() == 2 && node.leftChildRankDifference() == 2) {
                    node.demote();
                    rebalanceCount++;
                }
                return rebalanceCount;
            } else { //double rotate and fix ranks
                WAVLNode a = left.right;
                rotateLeftThenRight(node);
                a.rank += 2;
                left.demote();
                node.rank -= 2;
                rebalanceCount += 7;
                return rebalanceCount;
            }
        }
    }

    /**
     * this function remove all pointers to the node's in the tree and bypass the node.
     *
     * @param s assuming leaf WAVKNode or an Unary WAVLNode that will be removed from the tree.
     */
    private void removeNodeFromTree(WAVLNode s) {
        WAVLNode parent = s.parent;
        if (s.isARightChild()) {
            if (s.isLeafNode())
                parent.right = external;
            else {
                if (s.right.isExternalNode()) {
                    parent.right = s.left;
                    s.left.parent = parent;
                } else {
                    parent.right = s.right;
                    s.right.parent = parent;
                }
            }
        } else {
            if (s.isLeafNode())
                parent.left = external;
            else {
                if (s.right.isExternalNode()) {
                    parent.left = s.left;
                    s.left.parent = parent;
                } else {
                    parent.left = s.right;
                    s.right.parent = parent;
                }
            }
        }
    }

    /**
     * @param node - the node that this method returns its successor in the tree
     * @return the successor of node in the tree. or null if node has the largest key in the tree.
     */
    private WAVLNode findSuccessor(WAVLNode node) {
        // if node has right son go right then go all the way lefft
        if (!node.right.isExternalNode())
            return minimumNode(node.right);

        // go up until the first turn right
        WAVLNode y = node.parent;
        while (y != null && node == y.right) {
            node = y;
            y = node.parent;
        }
        return y;
    }

    /**
     * @param node is the root of a subtree in which we return the node with the minimum key in. assuming node is not null.
     * @return WAVLNode with the minimum key in the subtree of node.
     */
    private WAVLNode minimumNode(WAVLNode node) {
        //go all the way left from node to get to the minimum
        while (!node.left.isExternalNode()) {
            node = node.left;
        }

        return node;
    }


    /**
     * searches for a key in the tree, if no node with such ket exisits, returns the insertion point for the key
     *
     * @param key - the key to look for in the tree
     * @return a node with the specified key if one exists, and the insertion point for the key otherwise
     */
    private WAVLNode findNearestNode(int key) {
        // if node is an empty tree or external node than return null
        WAVLNode node = root;
        if (node == null || node.isExternalNode()) {
            return null;
        }

        while (true) {
            //if key found return node
            if (node.key == key) {
                return node;
            }

            //if nodes key is bigger than requested tree, try to go right, else try to go left
            //if son is external return node as insertion point
            if (node.key > key) {
                if (node.left.isExternalNode()) {
                    return node;
                }
                node = node.left;
            } else {
                if (node.right.isExternalNode()) {
                    return node;
                }
                node = node.right;
            }
        }
    }

    /**
     * public String min()
     *
     * Returns the info of the item with the smallest key in the tree, or null
     * if the tree is empty
     */
    public String min() {
        return min.info;
    }

    /**
     * public String max()
     *
     * Returns the info of the item with the largest key in the tree, or null if
     * the tree is empty
     */
    public String max() {
        return max.info;
    }

    /**
     * public int[] keysToArray()
     *
     * Returns a sorted array which contains all keys in the tree, or an empty
     * array if the tree is empty.
     */
    public int[] keysToArray() {
        if (empty()) {
            return new int[0];
        }

        int[] keys = new int[size];
        keysToArrayRecursive(root, keys, 0);
        return keys;
    }

    /**
     * Inserts the keys of the nodes in the subtree of a node, into an array at a specified index
     *
     * @param node  - The node whose subtree is to be inserted into the array
     * @param array - The array that the keys are inserted into
     * @param index - The index at which the keys are inserted
     * @return The last index where a key was inserted
     */
    private int keysToArrayRecursive(WAVLNode node, int[] array, int index) {
        if (node.isExternalNode()) {
            return index;
        }

        index = keysToArrayRecursive(node.left, array, index);

        array[index++] = node.key;

        return keysToArrayRecursive(node.right, array, index);
    }

    /**
     * public String[] infoToArray()
     *
     * Returns an array which contains all info in the tree, sorted by their
     * respective keys, or an empty array if the tree is empty.
     */
    public String[] infoToArray() {
        if (empty()) {
            return new String[0];
        }

        String[] arr = new String[size];
        infoToArrayRecursive(root, arr, 0);
        return arr;
    }

    /**
     * Inserts the infos of the subtree of node into an array at a specific index inorder
     *
     * @param node  - The node whose subtree is inserted into the array
     * @param array - The array that the infos are inserted into
     * @param index The index at which the infos are inserted
     * @return The last index where infos were inserted
     */
    private int infoToArrayRecursive(WAVLNode node, String[] array, int index) {
        if (node.isExternalNode()) {
            return index;
        }

        index = infoToArrayRecursive(node.left, array, index);

        array[index++] = node.info;

        return infoToArrayRecursive(node.right, array, index);
    }

    /**
     * Rotates to the left the right son of node
     *
     * @param node - the node whose son is to be rotated
     */
    private void rotateLeft(WAVLNode node) {
        WAVLNode rotatedNode = node.right;
        if (node.parent != null) {
            if (node.isARightChild()) {
                node.parent.right = rotatedNode;
            } else {
                node.parent.left = rotatedNode;
            }
        }

        if (root == node) {
            root = rotatedNode;
        }

        rotatedNode.parent = node.parent;
        node.setParent(rotatedNode);

        node.right = rotatedNode.left;
        node.right.setParent(node);

        rotatedNode.left = node;
    }

    /**
     * Rotates to the right the left son of node
     *
     * @param node - The node whose son is to be rotated
     */
    private void rotateRight(WAVLNode node) {
        WAVLNode rotatedNode = node.left;
        if (node.parent != null) {
            if (node.isARightChild()) {
                node.parent.right = rotatedNode;
            } else {
                node.parent.left = rotatedNode;
            }
        }

        if (root == node) {
            root = rotatedNode;
        }

        rotatedNode.parent = node.parent;
        node.setParent(rotatedNode);

        node.left = rotatedNode.right;
        node.left.setParent(node);

        rotatedNode.right = node;
    }

    /**
     * Rotates to the left the left right grandson of node,
     * then rotates to the right the left son of node
     */
    private void rotateLeftThenRight(WAVLNode node) {
        rotateLeft(node.left);
        rotateRight(node);
    }

    /**
     * Rotates to the right the right left grandson of node,
     * then rotate to the left the right son of node
     */
    private void rotateRightThenLeft(WAVLNode node) {
        rotateRight(node.right);
        rotateLeft(node);
    }

    /**
     * public int size()
     *
     * Returns the number of nodes in the tree.
     *
     * precondition: none postcondition: none
     */
    public int size() {
        return size;
    }

    /**
     * private class WAVLNode
     */
    private class WAVLNode {

        public int key;
        public String info;
        public int rank;
        public WAVLNode parent;
        public WAVLNode left;
        public WAVLNode right;

        /**
         * Creating External node
         */
        public WAVLNode() {
            this.rank = -1;
        }

        /**
         * Constructor for a new leaf node.
         */
        public WAVLNode(int key, String info, WAVLNode parent) {
            this.key = key;
            this.info = info;
            this.rank = 0;
            this.parent = parent;
            this.right = external;
            this.left = external;
        }

        public boolean isExternalNode() {
            return rank == -1;
        }

        /**
         * @return true if this node is a leaf and false otherwise
         */
        public boolean isLeafNode() {
            return left == external && right == external;
        }

        /**
         * @return true if this node has only one son that isn;t external
         */
        public boolean isUnaryNode() {
            return (this.left.isExternalNode() ^ this.right.isExternalNode());
        }

        /**
         * @return -1 if this.parent is null and the rank difference between this and its parent otherwise.
         */
        public int rankDifference() {
            if (this.parent == null)
                return -1;
            else
                return this.parent.rank - this.rank;
        }

        /**
         * assuming this.parent is not null
         *
         * @return true if this node is a right child of its parent and false otherwise.
         */
        public boolean isARightChild() {
            return this.parent.right == this;
        }

        /**
         * assuming left is not external leaf
         *
         * @return the rank difference between this node and its left child
         */
        public int leftChildRankDifference() {
            return this.rank - this.left.rank;
        }

        /**
         * assuming right is not external leaf
         *
         * @return the rank difference between this node and its right child
         */
        public int rightChildRankDifference() {
            return this.rank - this.right.rank;
        }

        /**
         * sets the parent of this node to parent if this node isn't external
         *
         * @param parent
         */
        public void setParent(WAVLNode parent) {
            if (!isExternalNode()) {
                this.parent = parent;
            }
        }


        public void promote() {
            rank++;
        }

        public void demote() {
            rank--;
        }

        private void deleteFields() {
            this.left = null;
            this.right = null;
            this.parent = null;
            this.info = null;
        }
    }
}
