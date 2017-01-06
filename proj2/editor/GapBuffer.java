package editor;
import java.util.*;


/*
Site: http://scienceblogs.com/goodmath/2009/02/18/gap-buffers-or-why-bother-with-1/
*/

public class GapBuffer{

	private int size;
	private int start;
	private int end;
	private char[] buffer;

	/*
	int size: size of the buffer array
	char[] buffer: the arracy of chars
	int start: starting position of gap/cursor
	int end: last position of gap
	*/

	public GapBuffer(int s){
		size = s;
		buffer = new char[size];
		start = 0;
		end = size; //is this right??

	}

	/*
	Adds a character to the char array in constant time.
	Assigns an empty cell in the gap to the char value c.
	If the gap is size 0 (start == end) then the buffer doubles
	in size(2*size) and a new gap is created of size (size) at the cursor.

	*/
	public void add(char c){

		if(start == end){
			expand();
		}
		buffer[start] = c;
		start += 1;
	}

	/*
	Removes a character from char array in constant time.
	Backspace --> moves the start pointer back one and sets
	whatever was at the positoin to null char literal
	Expands gap by 1
	*/

	public void delete(char c){

		if(start != 0){
			start -= 1; 
			buffer[start] = '\0'; //null char literal
		}

	}

	/*
	Returns size of char array not including the buffer.
	*/
	public int getSize(){
	
		return size;
	}

	public int getGapSize(){

		return end-start;
	}

	/*
	The cursor is the starting position of the gap
	*/
	public int getCursor(){

		return start;
	}


	/*
	Adjusts the pointers of start and end.  Copies the values that was
	at end to the front of the gap and moves the pointers forward.
	*/
	public void moveCursorForward(){
		
		if(end < size){
			char temp = buffer[start-1];
			buffer[end-1] = temp;
			start += 1;
			end += 1;
		}
	}

	/*
	Adjusts the pointers of start and end.  Doesn't need to remove 
	the char value at start.  Moves the start and end pointers of the 
	back one
	*/
	public void moveCursorBack(){
		
		if(start > 0){
			char temp = buffer[end-1];
			end -= 1; //+=1?
			start -= 1;

		}

	}

	public String export(){

		char[] ans = new char[size-(end-start)];
		System.arraycopy(buffer, 0, ans, 0, start);
		// System.out.println(new String(ans));
		// System.out.println(end);
		// System.out.println(size-end);
		// System.out.println(buffer[start]);
		System.arraycopy(buffer, start, ans, start, (size-end));
		// System.out.println(new String(ans));
		return new String(ans);

	}


	/*
	Expands the buffer if the gap is 0.  Makes the size of the buffer --> size*2
	The new gap is size (size)
	Gap begins at the space where start == end
	*/
	public void expand(){

		int expandedSize = size*2;
		char[] newBuff = new char[expandedSize];
		System.out.println(new String(buffer));
		System.arraycopy(buffer, 0, newBuff, 0, start);
		System.out.println(new String(newBuff));
		System.out.println(end);
		System.out.println(start);
		System.out.println(expandedSize-end);
		System.arraycopy(buffer, end, newBuff, start, expandedSize-end);

		buffer = newBuff;
		size = expandedSize;

	}


}