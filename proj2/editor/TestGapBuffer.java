import org.junit.Test;
import static org.junit.Assert.*;

public class TestGapBuffer{

	@Test
	public void testAdd(){

		GapBuffer g = new GapBuffer(8);
		g.add('a');
		g.add('b');

		// System.out.println(g.export());
		// System.out.println(g.getSize());
		// System.out.println(g.getGapSize());
		// System.out.println(new String(ans));
		// System.out.println(g.export());	
		assertEquals("ab", g.export());	

	}

	@Test
	public void testCursor(){

		GapBuffer g = new GapBuffer(8);
		g.add('a');
		g.add('b');
		g.add('c');
		g.add('d');

		assertEquals(4, g.getCursor());

		assertEquals("abcd", g.export());
		
		g.moveCursorBack();
		assertEquals(3, g.getCursor());

		assertEquals("abcd", g.export());

		g.moveCursorForward();
		assertEquals(4, g.getCursor());

		assertEquals("abcd", g.export());

	}

	@Test
	public void testExpand(){

		GapBuffer g = new GapBuffer(4);
		g.add('a');
		g.add('b');
		g.add('c');
		g.add('d');

		assertEquals(4, g.getSize());

		g.add('e');

		assertEquals(8, g.getSize());
	}

	public static void main(String[] args) {
        jh61b.junit.textui.runClasses(TestGapBuffer.class);
    }

}