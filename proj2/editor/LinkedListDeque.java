package editor;


public class LinkedListDeque<Item>{

	public class Node{
		public Item item;
		public Node next;
		public Node prev;

		public Node(Item initial, Node left, Node right){
			item = initial;
			prev = left;
			next = right;
		}
	}

	public Node current;
	private int size;
    public Node first;
    public Node last;

	public LinkedListDeque(){
		size = 0;
		current = new Node(null, null, null);
        first = new Node(null, null, current);
        last = new Node(null, current, null);
        current.prev = first;
        current.next = last;
	}

    public Node getFirst(){
        return first;
    }

	public Item getCurrent(){
        if(current.prev != null) {
            return current.prev.item;
        }
        return null;
	}

    public Item retrieve(Node n){

        return n.item;
    }

    public void moveLeft(){

        Node back = current.prev;

        if(back != first){

            if(current.next != last){
                current.prev.next = current.next;
                current.next.prev = current.prev;
            }else{
                current.prev.next = last;
            }

            current.next = back;

            if(back.prev == first){
                current.prev = first;
                first.next = current;
            }else{
                back.prev.next = current;
                current.prev = back.prev;
            }
            back.prev = current;

        }
        else if(first.item != null){
            Node newFirst = new Node(null, null, first);
            first.prev = current;
            current.prev = newFirst;
            first.next = current.next;
            current.next.prev = first;
            current.next = first;
            newFirst.next = current;

            first = newFirst;

        }
    }

    public void moveRight(){
        Node front = current.next;
        if(first.item == null && current.prev == first && current.next != last){
            Node after = current.next;
//            first = after;
            current.next.next.prev = current;
            current.next = current.next.next;
            first = after;
            first.prev = null;
            first.next = current;
            current.prev = first;

        }
        else if(front != last){

            if(current.prev == first) {
                front.prev = first;
                first.next = front;
            }else{
                front.prev = current.prev;
                current.prev.next = front;
            }
            current.prev = front;
            if(front.next == last) {
                current.next = last;
                last.prev = current;
            }else{
                current.next = front.next;
                current.next.prev = current;
            }
            front.next = current;

        }

    }


    public void addCurrent(Item it){

        size++;
        if(first.item == null) {
            first.item = it;
            return;
//        }else if(current.prev == first && first.item != null && current.next != last){
//            Node newNode = new Node(it, null, first);
//            first.prev = newNode;
//            first = newNode;
        }else {
            Node newNode = new Node(it, current.prev, current);
            current.prev.next = newNode;
            current.prev = newNode;
        }
    }

    public Item removeCurrent(){


        if(current.prev == first){
            if(first.item != null) {
                Item it = first.item;
                first.item = null;

                return it;
            }
            System.out.println("ERROR");
            return null;
        }
        size--;
        Node old = current.prev;
        if(old.prev == null){
            current.prev = null;
            return old.item;
        }
        old.prev.next = current;
        current.prev = old.prev;

        return old.item;

    }

	public boolean isEmpty(){
		if(size == 0){
			return true;
		}
		return false;
	}
	public int size(){
		return size;
	}

	public Item get(int index){
		Node tempNode = current.next;
		while(index != 0){
            if(tempNode == current){
                tempNode = current.next;
            }
			tempNode = tempNode.next;
			if(tempNode.item == null){
				return null;
			}
			index--;
		}
		return tempNode.item;

	}

    public void insertAtNode(Item it, Node n){
        Node insert = new Node(it, n.prev, n.next);
        n.prev.next = insert;
        n.next.prev = insert;


    }

    public void moveCurrent(Node n){
        current.prev.next = current.next;
        current.next.prev = current.prev;

        if(n == first && first.item != null){
            Node newFirst = new Node(null, null, current);
            first.prev = current;
//            first.next = current.next;
            current.next = first;
            current.prev = newFirst;
            first = newFirst;

        }else {
            n.next.prev = current;
            current.next = n.next;
            current.prev = n;
            n.next = current;
        }
    }

    public Node findFirst() {

        Node ans;
        if (current.prev == null) {
            ans = current.next;
        } else {
            ans = current.prev;
            while (ans.prev != null) {
                ans = ans.prev;
            }

        }
        return ans;
    }

}
