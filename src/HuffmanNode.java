public class HuffmanNode {
    HuffmanNode left, right;
    String ch;
    int freq;
    HuffmanNode(){
        this.left = this.right = null;
        ch = "";
        freq=0;
    }
    HuffmanNode(HuffmanNode left, HuffmanNode right, String ch, int freq){
        this.left = left;
        this.right = right;
        this.ch = ch;
        this.freq = freq;
    }
    public boolean isLeaf(){return this.left==null && this.right ==null;}
}
