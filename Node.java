class Node {
    String feature;
    double threshold;
    Node left;
    Node right;
    String label;

    Node(String feature, double threshold) {
        this.feature = feature;
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
