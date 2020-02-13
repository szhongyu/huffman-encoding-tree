import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class CharCounterTest{
	CharCounter cc = new CharCounter();
	@Test
	public void testCharCounter(){
		assertEquals(0,cc.getTable().keySet().size());
	}

	@Test
	public void testGetCount() throws Exception {
		InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		cc.countAll(ins);
		int key = 't';
		assertEquals(3,cc.getCount(key));
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCount2(){
		cc.getCount(-1);
	}
	@Test(expected = IllegalStateException.class)
	public void testGetCount3(){
		cc.getCount(12345);
	}

	@Test
	public void testCountAll() throws Exception{
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		cc.countAll(ins2);
		int key = 't';
		assertEquals(3,cc.getCount(key));
	}

	@Test
	public void testCountAll2(){
		InputStream ins2 = null;
		try {
			cc.countAll(ins2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
	
	@Test
	public void testAdd() {
		cc.add(0);
		assertEquals(1,cc.getCount(0));
	}

	@Test
	public void testSet() {
		cc.set(0,2);
		assertEquals(2,cc.getCount(0));
	}

	@Test
	public void testClear() throws Exception{
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		cc.countAll(ins2);
		cc.clear();
		int key = 't';
		assertEquals(0,cc.getCount(key));
	}

	@Test
	public void testGetTable() throws Exception{
		InputStream ins2 = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		cc.countAll(ins2);
		assertEquals(7,cc.getTable().keySet().size());
	}

}
