import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CharCounter implements ICharCounter,IHuffConstants{
	
	Map<Integer, Integer> storage;
	
	public CharCounter() {
		storage = new HashMap<Integer,Integer>();
	}

	 /**
     * Returns the count associated with specified character.
     * @param ch is the chunk/character for which count is requested
     * @return count of specified chunk
     * @throws the appropriate exception if ch isn't a valid chunk/character
     */
    public int getCount(int ch) {
    	if(ch<0 || ch>ALPH_SIZE) {
    		throw new IllegalStateException("ch does not exist");
    	}
    	return storage.getOrDefault(ch, 0);
    	
    }

    /**
     * Initialize state by counting bits/chunks in a stream
     * @param stream is source of data
     * @return count of all chunks/read
     * @throws IOException if reading fails
     */
    public int countAll(InputStream stream) throws IOException{
    if(stream==null) {
    	throw new IOException("stream is null");
    }
    int counter = 0;
    while(true) {
    	int flag = stream.read();
    	if(flag==-1) {
    		break;
    	}
    	counter++;
    	add(flag);
    }
    stream.close();
    return counter;
    }
    /**
     * Update state to record one occurrence of specified chunk/character.
     * @param i is the chunk being recorded
     */
    public void add(int i) {
    	storage.put(i,storage.getOrDefault(i,0)+1);
    }
    
    /**
     * Set the value/count associated with a specific character/chunk.
     * @param i is the chunk/character whose count is specified
     * @param value is # occurrences of specified chunk
     */
    public void set(int i, int value) {
    	storage.put(i, value);
    }
    
    /**
     * All counts cleared to zero.
     */
    public void clear() {
    	Set<Integer> theKeys = storage.keySet();
    	for(int each: theKeys) {
    		storage.put(each, 0);
    	}
    }
    
    /**
     * @return a map of all characters and their frequency
     */
    public Map<Integer, Integer> getTable(){
    	return storage;
    }
}
