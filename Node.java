class Node {
    int featureIndex;
    double threshold;
    Node left;
    Node right;
    String label;

    Node(int featureIndex, double threshold) {
        this.featureIndex = featureIndex;
        this.threshold = threshold;
        this.left = null;
        this.right = null;
        this.label = null;
    }

    Node(String label) {
        this.label = label;
    }

    boolean isLeaf() {
        return label != null;
    }
}
