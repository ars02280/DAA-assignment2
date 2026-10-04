package dataStr;

public class MyLinkedList {

    private int size;
    private Node head;
    private long steps = 0;
    private long moves = 0;
    private long comparisons = 0;



    private static class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
            this.next = null;
        }
    }

   public void add(int x){
       Node newNode = new Node(x);
       if (head == null){
           head = newNode;
           moves++;
       }
       else{
          Node current= head;
          while (current.next != null){
              steps++;

               current = current.next;
          }
          current.next = newNode;
          moves++;

       }
       size++;
    }



    public int get(int index){
        if (index >= size || index < 0){
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node current = head;
        for (int i =0;i < index;i++){
            current = current.next;
            steps++;
        }
        int elem = current.value;
        return elem;
    }

    public boolean contains(int x){

        Node current = head;
        for (int i =0;i < size ;i++){
            steps++;
            comparisons++;
            if (current.value ==x){return true;}
            current = current.next;
        }
        return false;
    }

public int remove(int index){
    if (index >= size || index < 0){
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }
    if(index ==0){
        int removedValue = head.value;
        head = head.next;
        moves++;
        size--;
        return removedValue;
    }
    if (index > 0){
        Node current = head;

        for (int i =0;i < index - 1;i++){
            current = current.next;
            steps++;
        }
        steps++;
        int elem = current.next.value;

        Node prev = current;
        prev.next = prev.next.next;
        moves++;
        size--;
        return elem;
    }
    return -1;
}

public  void  add(int index, int x){
    if (index > size || index < 0){
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }
    if(index ==0){
        Node newNode = new Node(x);
        newNode.next = head;
        head = newNode;
        moves++;
        size++;
    }
    if(index > 0 ){
        Node current = head;
        for (int i =0;i < index - 1;i++){
            current = current.next;
            steps++;
        }
        Node newNode = new Node(x);
        newNode.next = current.next;
        current.next = newNode;
        size++;
        moves++;
        steps++;


    }


    }

















}
