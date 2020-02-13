import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class HuffTest {
	
	@Test
	public void testHuff() {
		Huff test = new Huff();
	}
	
	@Test
	public void testMakeHuffTree() throws Exception{
		Huff check = new Huff();
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		IHuffBaseNode base = check.makeHuffTree(ins2).root();
		HuffInternalNode base2 = (HuffInternalNode)base;
		assertEquals(11,base2.weight());
	}
	
	@Test
	public void testMakeHuffTree2(){
		Huff check = new Huff();
		InputStream ins2 = null;
		try {
			check.makeHuffTree(ins2);
		}
		catch (IOException e){
			
		}
	}

	@Test
	public void testMakeTable() throws Exception{
		Huff check = new Huff();
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		check.makeHuffTree(ins2);
		assertTrue("1111".equals(check.makeTable().get(256)));
	}

	@Test
	public void testGetCode() throws Exception{
		Huff check = new Huff();
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		check.makeHuffTree(ins2);
		check.makeTable();
		assertTrue("1111".equals(check.getCode(256)));
	}
	
	@Test(expected = IllegalStateException.class)
	public void testGetCode2(){
		Huff check = new Huff();
		assertTrue("1111".equals(check.getCode(256)));
	}

	@Test
	public void testShowCounts() throws Exception{
		Huff check = new Huff();
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		check.makeHuffTree(ins2);
		int key = 't';
		assertEquals(3,(int)check.showCounts().get(key));
	}

	@Test
	public void testWrite() throws Exception{
		Huff check = new Huff();
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		check.makeHuffTree(ins2);
		check.makeTable();
		assertEquals(797,check.write("test.txt", "middle.txt", true));
	}
	
	@Test
	public void testWrite2() throws Exception{
		Huff check = new Huff();
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		check.makeHuffTree(ins2);
		check.makeTable();
		assertEquals(797,check.write("test.txt", "middle.txt", false));
	}
	
	@Test
	public void testWrite3() throws Exception{
		Huff check = new Huff();
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		check.makeHuffTree(ins2);
		check.makeTable();
		assertEquals(0,check.write("test2.txt", "middle2.txt", false));
	}

	@Test
	public void testUncompress() throws Exception{
		Huff check = new Huff();
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		check.makeHuffTree(ins2);
		check.makeTable();
		check.write("test.txt", "middle.txt", true);
		assertEquals(720,check.uncompress("middle.txt", "last.txt"));
	}

	@Test
	public void testHeaderSize() throws Exception{
		Huff check = new Huff();
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		check.makeHuffTree(ins2);
		check.makeTable();
		assertEquals(119,check.headerSize());
	}

	@Test
	public void testWriteHeader() throws Exception{
		Huff check = new Huff();
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		check.makeHuffTree(ins2);
		ByteArrayOutputStream out = new ByteArrayOutputStream(); 
		assertEquals(128, check.writeHeader(new BitOutputStream(out)));
		out.close(); 
	}

	@Test
	public void testReadHeader() throws Exception{
		Huff check = new Huff();
		BitOutputStream out = new BitOutputStream("random.txt"); 
		check.writeHeader(out);
		BitInputStream in = new BitInputStream("middle.txt"); 
		IHuffBaseNode base = check.readHeader(in).root();
		HuffInternalNode base2 = (HuffInternalNode)base;
		assertEquals(0,base2.weight());
		BitInputStream in2 = new BitInputStream("middle.txt"); 
		assertEquals(329,check.readHeader(in2).size());
	}
	
	@Test
	public void testReadHeader2(){
		try {
			Huff check = new Huff();
			BitOutputStream out = new BitOutputStream("random.txt"); 
			check.writeHeader(out);
			BitInputStream in = new BitInputStream("middle.txt"); 
			IHuffBaseNode base = check.readHeader(in).root();
			HuffInternalNode base2 = (HuffInternalNode)base;
			assertEquals(0,base2.weight());
			assertEquals(0,check.readHeader(in).size());
		}
		catch (IOException e){
			
		}
	}
	
	@Test
	public void testReadHeader3(){
		try {
			Huff check = new Huff();
			BitOutputStream out = new BitOutputStream("random.txt"); 
			check.writeHeader(out);
			BitInputStream in = null; 
			IHuffBaseNode base = check.readHeader(in).root();
		}
		catch (IOException e){
			
		}
	}
	

}
