import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
public class Huff implements IHuffEncoder, ITreeMaker,IHuffHeader,IHuffModel{
	
	private Map<Integer,String> encode;
	private boolean flag = false;
	private Map<Integer,Integer> fre;
	private static MinHeap Hheap; 
	private static HuffTree tree;
	private int writeCharCounter;
	
	public Huff() {
		encode = new HashMap<Integer,String>();
		fre = new HashMap<Integer,Integer>();
	}
	/**
     * Initialize state from a tree, the tree is obtained
     * from the treeMaker.
     * @return the map of chars/encoding
     */
    public Map<Integer, String> makeTable(){
    	flag = true;
    	IHuffBaseNode copy = tree.root();
    	traverse(copy,"");
    	return encode;
    }
    
    private void traverse(IHuffBaseNode root,String initial) {
    	if(root instanceof HuffLeafNode) {
    		HuffLeafNode leaf = (HuffLeafNode) root;
    		encode.put(leaf.element(), initial);
    	}
    	else{
    	HuffInternalNode internal = (HuffInternalNode)root;
    	traverse(internal.left(),initial+0);
    	traverse(internal.right(),initial+1);
    	}
    }
    
    public HuffTree makeHuffTree(InputStream stream) throws IOException{
    	if(stream==null) {
    		throw new IOException("stream is null");
    	}
    	CharCounter countChars  = new CharCounter();
    	countChars.countAll(stream);
    	fre = countChars.getTable();
    	int size = fre.keySet().size();
    	HuffTree[] treeArray = new HuffTree[size+1];
    	for(int i = 0;i<size+1;i++) {
    		treeArray[i] = new HuffTree(null,null,0);
    	}
    	Hheap = new MinHeap(treeArray,0,size+1);
    	Set<Integer> keys = fre.keySet();
    	for(Integer each: keys) {
    		Hheap.insert(new HuffTree(each,fre.get(each)));
    	}
    	Hheap.insert(new HuffTree(PSEUDO_EOF,1));
    	HuffTree toBuild =  buildTree();
    	tree = toBuild;
    	return toBuild;
    }
    
    private HuffTree buildTree() {

    	HuffTree combine = null;

    	  while (Hheap.heapsize() > 1) { // While two items left
    	    HuffTree tmp1 = (HuffTree) Hheap.removemin();
    	    HuffTree tmp2 = (HuffTree) Hheap.removemin();
    	    combine = new HuffTree(tmp1.root(), tmp2.root(),tmp1.weight() + tmp2.weight());
    	    Hheap.insert(combine);   // Return new tree to heap
    	  }
    	  return combine;            // Return the tree	
        }  
    /**
     * Returns coding, e.g., "010111" for specified chunk/character. It
     * is an error to call this method before makeTable has been
     * called.
     * @param i is the chunk for which the coding is returned
     * @return the huff encoding for the specified chunk
     */
    public String getCode(int i) {
    	if(flag==false) {
    		throw new IllegalStateException();
    	}
    	return encode.get(i);
    }
    
    
    /**
     * @return a map of all characters and their frequency
     */
    public Map<Integer, Integer> showCounts(){
    	return fre;
    }
    
    /**
     * Write a compressed version of the data read by the InputStream parameter,
     * -- if the stream is not the same as the stream last passed to initialize,
     * then compression won't be optimal, but will still work. If force is
     * false, compression only occurs if it saves space. If force is true
     * compression results even if no bits are saved.
     * 
     * @param inFile is the input stream to be compressed
     * @param outFile   specifies the OutputStream/file to be written with compressed data
     * @param force  indicates if compression forced
     * @return the size of the compressed file
     */
    public int write(String inFile, String outFile, boolean force){
		int compressLength = 0;
		int initialLength = 0;
		try {
			BitInputStream input = new BitInputStream(inFile);
			BitOutputStream output = new BitOutputStream(outFile);
			makeHuffTree(input);
			compressLength += writeHeader(output);
			makeTable();
			Set<Integer> keys = showCounts().keySet();
			for(int each: keys) {
				initialLength+=showCounts().get(each)*BITS_PER_WORD;
				compressLength+=showCounts().get(each)*getCode(each).length();
			}
			
			compressLength+=getCode(PSEUDO_EOF).length();
			if(!force&&initialLength>compressLength) {
				return 0;
			}
			input = new BitInputStream(inFile);
			while(true) {
				int current = input.read(BITS_PER_WORD);
				if(current==-1) {
					break;
				}
				String encodeString  = encode.get(current);
				for(int i = 0; i < encodeString .length(); i++) {
					output.write(1, encodeString .charAt(i) - '0');
				}
			}
			String EndOfFile  = encode.get(PSEUDO_EOF);
			for(int i = 0; i < EndOfFile .length(); i++) {
				output.write(1, EndOfFile .charAt(i) - '0');
			}
			output.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compressLength;
       }

    /**
     * Uncompress a previously compressed file.
     * 
     * @param inFile  is the compressed file to be uncompressed
     * @param outFile is where the uncompressed bits will be written
     * @return the size of the uncompressed file
     */
    public int uncompress(String inFile, String outFile) {
		int length = 0;
		try {
			BitInputStream input = new BitInputStream(inFile);
			BitOutputStream output = new BitOutputStream(outFile);
			tree = readHeader(input);
			makeTable();
			int bits;
			IHuffBaseNode base = tree.root();
			while(true) {
				bits = input.read(1);
				  //System.out.println(bits);
				  if (bits == -1)
				  {
				     throw new IOException("unexpected end of input file");
				  }
				  else
				  { 
				     if ( (bits & 1) == 0) { // read a 0, go left
				    	 HuffInternalNode current = (HuffInternalNode)base;
				    	 base = current.left();
				    	 }
				     else {// read a 1, go right
				    	 HuffInternalNode current = (HuffInternalNode)base;
				    	 base = current.right();
				     } 

				    if (base.isLeaf())
				    {
				    	HuffLeafNode current = (HuffLeafNode) base;
				      int element = current.element();
				      if (element==PSEUDO_EOF) 
				        break; // out of loop
				      else {
				    	  output.write(BITS_PER_WORD, element);
				    	  length += BITS_PER_WORD;
				      }
				      base = tree.root();
				    }
				  }
			}
			input.close();
			output.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
    	return length;
    }
    

    /**
     * The number of bits in the header using the implementation, including
     * the magic number presumably stored.
     * @return the number of bits in the header
     */
    public int headerSize() {
    	return tree.size()+BITS_PER_INT;
    }
    
    /**
     * Write the header, including magic number and all bits needed to
     * reconstruct a tree, e.g., using <code>readHeader</code>.
     * @param out is where the header is written
     * @return the size of the header 
     */
    public int writeHeader(BitOutputStream out) {
    	out.write(BITS_PER_INT, MAGIC_NUMBER);
    	IHuffBaseNode node = tree.root();
    	preOrder(node,out);
    	out.write(BITS_PER_WORD+1, PSEUDO_EOF);
    	return BITS_PER_INT+writeCharCounter+BITS_PER_WORD+1;
    }
    
    private void preOrder(IHuffBaseNode node,BitOutputStream out) {
    	if(node instanceof HuffInternalNode) {
    		HuffInternalNode copy = (HuffInternalNode) node;
    		out.write(1, '0'-'0');
    		writeCharCounter++;
    		preOrder(copy.left(),out);
    		preOrder(copy.right(),out);
    	}
    	else {
    		HuffLeafNode copy = (HuffLeafNode) node;
    		out.write(1, '1'-'0');
    		writeCharCounter++;
    		String toadd = convertToBinary(copy.element());
    		if(copy.element()!=PSEUDO_EOF) {
    			toadd = "0"+toadd.substring(1, toadd.length());
    		}
    		//System.out.println(toadd);
    		for(int i = 0;i<toadd.length();i++) {
    			out.write(1, toadd.charAt(i)-'0');
    			writeCharCounter++;
    		}
    	}
    }
    
    
    private String convertToBinary(int target) {
    	String initial = Integer.toBinaryString(0x100|target);
    	return initial;
    }
    
    /**
     * Read the header and return an ITreeMaker object corresponding to
     * the information/header read.
     * @param in is source of bits for header
     * @return an ITreeMaker object representing the tree stored in the header
     * @throws IOException if the header is bad, e.g., wrong MAGIC_NUMBER, wrong
     * number of bits, I/O error occurs reading
     */
    public HuffTree readHeader(BitInputStream in) throws IOException{
    	if(in==null) {
    		throw new IOException("Input stream not found");
    	}
    	int magic = in.read(BITS_PER_INT);
    	if (magic != MAGIC_NUMBER){
    	   throw new IOException("magic number not right");
    	}
    	HuffTree toReturn =  new HuffTree(null, null,0);
    	int bit = in.read(1);
    	if((bit&1)==0) {
    		HuffInternalNode current = new HuffInternalNode(null,null,0);
    		readTraverse(current,in);
    		toReturn.setRoot(current);
    	}
    	else if((bit&1)==1){
    	    HuffLeafNode current = new HuffLeafNode(in.read(BITS_PER_WORD+1),0);
    	    toReturn.setRoot(current);
    	}
    	else {
    		throw new IOException("tree construct not correct");
    	}
    	if(in.read(BITS_PER_WORD+1)!=PSEUDO_EOF) {
    		throw new IOException("PSEUDO_EOF not found");
    	}
		return toReturn;
    }
    
    private void readTraverse(IHuffBaseNode target, BitInputStream in) throws IOException{
    	int bit = in.read(1);
    	HuffInternalNode root = (HuffInternalNode) target;
    	if((bit&1)==0) {
    		IHuffBaseNode current = new HuffInternalNode(null,null,0);
    		root.setLeft(current);
    		readTraverse(current,in);
    	}
    	else if((bit&1)==1){
    		IHuffBaseNode current = new HuffLeafNode(in.read(BITS_PER_WORD+1),0);
    	    root.setLeft(current);
    	}
    	else {
    		throw new IOException("tree construct not correct");
    	}
    	bit = in.read(1);
    	if((bit&1)==0) {
    		IHuffBaseNode current = new HuffInternalNode(null,null,0);
    		root.setRight(current);
    		readTraverse(current,in);
    	}
    	else if((bit&1)==1){
    		IHuffBaseNode current = new HuffLeafNode(in.read(BITS_PER_WORD+1),0);
    	    root.setRight(current);
    	}
    	else {
    		throw new IOException("tree construct not correct");
    	}
    } 
    
    //public static void main(String[] args) throws Exception {
    		//ICharCounter cc = new CharCounter();
    		//InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
    		//cc.countAll(ins);
    		//System.out.println(cc.getTable());
    		//Huff check = new Huff();
    		//InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
    		//check.makeHuffTree(ins2);
    		//System.out.println(tree.weight());
    		//System.out.println(check.makeTable());
    		//BitOutputStream a = new BitOutputStream("123");
    		//check.writeHeader(a);
    		//Huff check = new Huff();
    		//check.write("test2.txt","middle2.txt",true);
    		//check.uncompress("middle2.txt","last2.txt");
    		//}
}
