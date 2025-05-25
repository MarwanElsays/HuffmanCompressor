public class HuffmanNode {
    HuffmanNode left, right;
    ByteArrayWrapper ch;
    int freq;

    HuffmanNode() {
        this.left = this.right = null;
        ch = null;
        freq = 0;
    }

    HuffmanNode(HuffmanNode left, HuffmanNode right, ByteArrayWrapper ch, int freq) {
        this.left = left;
        this.right = right;
        this.ch = ch;
        this.freq = freq;
    }

    public boolean isLeaf() {
        return this.left == null && this.right == null;
    }
}
